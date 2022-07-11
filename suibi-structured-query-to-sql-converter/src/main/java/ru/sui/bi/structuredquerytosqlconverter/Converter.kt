package ru.sui.bi.structuredquerytosqlconverter

import ru.sui.bi.structuredquerytosqlconverter.exception.ConversionException
import ru.sui.bi.structuredquerytosqlconverter.model.*

class Converter(
    private val converterDialectHelperRegistry: ConverterDialectHelperRegistry,
    private val converterMetaHelperFactory: ConverterMetaHelperFactory
) {

    fun convert(structuredQuery: StructuredQuery): String {
        validate(structuredQuery)

        val sourceTableId = structuredQuery.query.sourceTable
        val fields = structuredQuery.query.fields
        val joins = structuredQuery.query.joins
        val aggregation = structuredQuery.query.aggregation
        val groupBy = structuredQuery.query.groupBy
        val filter = structuredQuery.query.filter
        val orderBy = structuredQuery.query.orderBy
        val limit = structuredQuery.query.limit

        // Определяем sourceTable
        val sourceTable = converterMetaHelperFactory.getTableInfo(sourceTableId)
        val sourceTableAlias = sourceTable.tableName

        // Создаем хелперы
        val dialectHelper = converterDialectHelperRegistry.get()
        val metaHelper = createMetaHelper(sourceTableId, sourceTableAlias, joins)

        // Создаем запрос
        val sqlQuery = dialectHelper.createQuery()

        // FIELDS
        fields?.forEach {
            val alias = it.joinAlias ?: sourceTableAlias
            val column = metaHelper.getColumn(alias, it.field)
            dialectHelper.selectField(sqlQuery, alias, column)
        }

        if (fields == null && groupBy == null && aggregation == null) {
            dialectHelper.selectAll(sqlQuery)
        }

        // AGGREGATION
        aggregation?.forEach {
            if (it.field == null) {
                dialectHelper.addAggregation(sqlQuery, it.aggFunction, it.fieldAlias)
            } else {
                val alias = it.joinAlias ?: sourceTableAlias
                val column = metaHelper.getColumn(alias, it.field)
                dialectHelper.addAggregation(sqlQuery, it.aggFunction, alias, column, it.fieldAlias)
            }
        }

        // FROM
        dialectHelper.setFrom(sqlQuery, sourceTable)

        // JOINS
        joins?.forEach { join ->
            val joinAlias = join.alias
            val joinTable = metaHelper.getTable(joinAlias)
            val joinLeftAlias = join.leftOn.alias ?: sourceTableAlias
            val joinRightAlias = join.rightOn.alias ?: sourceTableAlias

            dialectHelper.addJoin(
                query = sqlQuery,
                strategy = join.strategy ?: Join.Strategy.LEFT_JOIN,
                table = joinTable,
                alias = joinAlias,
                condition = dialectHelper.createSimpleJoinCondition(
                    leftAlias = joinLeftAlias,
                    leftColumn = metaHelper.getColumn(joinLeftAlias, join.leftOn.field),
                    rightAlias = joinRightAlias,
                    rightColumn = metaHelper.getColumn(joinRightAlias, join.rightOn.field)
                )
            )
        }

        // WHERE
        if (filter != null) {
            val condition = createCondition(filter) {
                val alias = it.joinAlias ?: sourceTableAlias
                val column = metaHelper.getColumn(alias, it.field)
                return@createCondition dialectHelper.createPredicate(alias, column, it.operation, it.value)
            }

            dialectHelper.setWhere(sqlQuery, condition)
        }

        // GROUP BY
        groupBy?.forEach {
            val alias = it.joinAlias ?: sourceTableAlias
            val column = metaHelper.getColumn(alias, it.field)
            dialectHelper.selectField(sqlQuery, alias, column)
            dialectHelper.addGroupBy(sqlQuery, alias, column)
        }

        // ORDER BY
        orderBy?.forEach {
            val direction = it.order ?: OrderBy.Direction.ASC

            if (it.fieldAlias != null) {
                dialectHelper.addOrderBy(sqlQuery, it.fieldAlias, direction)
            } else {
                val alias = it.joinAlias ?: sourceTableAlias
                val column = metaHelper.getColumn(alias, it.field!!)
                dialectHelper.addOrderBy(sqlQuery, alias, column, direction)
            }
        }

        // LIMIT
        if (limit != null) {
            sqlQuery.limit(limit)
        }

        return sqlQuery.toSql()
    }

    private fun validate(structuredQuery: StructuredQuery) {
        val query = structuredQuery.query

        // Query содержит объект group-by, но не содержит объекта aggregation
        if (!query.groupBy.isNullOrEmpty() && query.aggregation.isNullOrEmpty()) {
            throw ConversionException("Необходимо указать агрегации, т.к указаны группирующие поля")
        }

        // Валидируем порядок джоинов
        if (query.joins != null) {
            val availableAliases = mutableSetOf<String>()

            query.joins.forEach { join ->
                availableAliases.add(join.alias)

                listOfNotNull(join.leftOn.alias, join.rightOn.alias).forEach {
                    if (!availableAliases.contains(it)) {
                        throw ConversionException("Таблица с алиасом $it отсутствует или джоинится после использования")
                    }
                }
            }
        }

        // Проверяем, что fields пустой, если есть group-by
        if (!query.groupBy.isNullOrEmpty() && !query.fields.isNullOrEmpty()) {
            throw ConversionException("При наличии группировки перечень полей не указывается")
        }

        // Проверяем, что fields пустой, если есть aggregation
        if (!query.aggregation.isNullOrEmpty() && !query.fields.isNullOrEmpty()) {
            throw ConversionException("При наличии агрегации перечень полей не указывается")
        }

        // Проверяем, что в сортировках указан field или fieldAlias
        query.orderBy?.forEach {
            if (it.field == null && it.fieldAlias == null) {
                throw ConversionException("Сортировка на содержит \"field\" или \"field-alias\"")
            }
        }

        // Проверяем, что все fieldAlias входят в aggregation
        query.orderBy?.filter { it.fieldAlias != null }?.forEach { orderBy ->
            val allowed = (query.aggregation ?: emptyList()).any { it.fieldAlias == orderBy.fieldAlias }

            if (!allowed) {
                throw ConversionException("Поле \"field-alias\": ${orderBy.fieldAlias} отсутствует в агрегациях")
            }
        }
    }

    private fun createMetaHelper(sourceTableId: Long, sourceTableAlias: String, joins: List<Join>?): ConverterMetaHelper {
        val aliasMap = mutableMapOf<String, Long>()
            .plus(sourceTableAlias to sourceTableId)
            .plus(joins?.associate { it.alias to it.sourceTable } ?: emptyMap())

        return converterMetaHelperFactory.create(aliasMap)
    }

    private fun createCondition(filter: PredicateTree<Filter>, predicateCreator: (Filter) -> String): String {
        var result = filter.nodes
            .map {
                when (it) {
                    is PredicateTree.Node.SubTree -> createCondition(it.tree, predicateCreator)
                    is PredicateTree.Node.Value -> predicateCreator(it.value)
                }
            }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" ${filter.predicate.name} ", "( ", " )") { "( $it )" }
            ?: "( TRUE )"

        if (filter.not == true) {
            result = "( NOT $result )"
        }

        return result
    }

}