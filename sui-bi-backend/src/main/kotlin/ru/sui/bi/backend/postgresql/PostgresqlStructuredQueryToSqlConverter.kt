package ru.sui.bi.backend.postgresql

import io.zeko.db.sql.Query
import org.springframework.data.repository.findByIdOrNull
import ru.sui.bi.backend.core.exception.SuiBiException
import ru.sui.bi.backend.core.structuredquery.*
import ru.sui.bi.backend.extension.addField
import ru.sui.bi.backend.extension.currentTable
import ru.sui.bi.backend.jpa.entity.ColumnEntity
import ru.sui.bi.backend.jpa.entity.TableEntity
import ru.sui.bi.backend.jpa.repository.ColumnRepository
import ru.sui.bi.backend.jpa.repository.TableRepository

// Временная реализация, поэтому тупо все свалим сюда (и так сойдет)
class PostgresqlStructuredQueryToSqlConverter(
    // Костыли, т.к. StructuredQuery, передаваемый в клиенты содержит ИДшники (в будущем запрос будет раскрываться до передачи)
    private val tableRepository: TableRepository,
    private val columnRepository: ColumnRepository
) {

    fun convert(structuredQuery: StructuredQuery): String {
        // Определяем sourceTable
        val sourceTableId = structuredQuery.query.sourceTable
        val fields = structuredQuery.query.fields
        val joins = structuredQuery.query.joins
        val aggregation = structuredQuery.query.aggregation
        val groupBy = structuredQuery.query.groupBy
        val filter = structuredQuery.query.filter
        val orderBy = structuredQuery.query.orderBy
        val limit = structuredQuery.query.limit

        // Создаем хелперы
        val dialectHelper = createDialectHelper()
        val metaHelper = createMetaHelper()

        // Определяем sourceTable
        val sourceTable = metaHelper.getTable(sourceTableId)
        val sourceTableAlias = sourceTable.tableName

        // Добавляем алиасы в metaHelper
        metaHelper.addAlias(sourceTable.id!!, sourceTableAlias)
        joins?.forEach { metaHelper.addAlias(it.sourceTable, it.alias) }

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

    private fun createMetaHelper(): MetaHelper {
        return MetaHelper(tableRepository, columnRepository)
    }

    private fun createDialectHelper(): DialectHelper {
        return DialectHelper()
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

private class MetaHelper(
    private val tableRepository: TableRepository,
    private val columnRepository: ColumnRepository
) {

    private val aliasMap = mutableMapOf<String, Long>()

    fun addAlias(id: Long, alias: String) {
        aliasMap[alias] = id
    }

    fun getTable(id: Long): TableEntity {
        return tableRepository.findByIdOrNull(id) ?: throw SuiBiException("Не удалось найти таблицу с ИД = $id")
    }

    fun getTable(alias: String): TableEntity {
        val id = aliasMap[alias] ?: throw SuiBiException("Не удалось найти таблицу с алиасом = $alias")
        return getTable(id)
    }

    fun getColumn(tableId: Long, id: Long): ColumnEntity {
        return columnRepository.findByIdOrNull(id)
            ?.takeIf { it.table.id == tableId }
            ?: throw SuiBiException("Не удалось найти поле с ИД = $id в таблице с ИД = $tableId")
    }

    fun getColumn(tableAlias: String, id: Long): ColumnEntity {
        val tableId = aliasMap[tableAlias] ?: throw SuiBiException("Не удалось найти таблицу с алиасом = $tableAlias")
        return getColumn(tableId, id)
    }

}

private class DialectHelper {

    fun createQuery(): Query {
        // Отключаем багованное экранирование и странный функционал AS, делаем все руками
        return Query(espChar = "`~`~`~`~`~`", asChar = "`~`~`~`~`~`", espTableName = false)
    }

    fun setFrom(query: Query, table: TableEntity) {
        query.from(escapeName(table))
    }

    fun selectAll(query: Query) {
        query.fields("*")
    }

    fun selectField(query: Query, alias: String, column: ColumnEntity) {
        addFieldInternal(query, "${escapeName(alias)}.${escapeName(column.columnName)} AS ${escapeName(column.columnName)}")
    }

    fun addAggregation(query: Query, function: String, alias: String) {
        addAggregationInternal(query, function, "*", alias)
    }

    fun addAggregation(query: Query, function: String, columnAlias: String, column: ColumnEntity, alias: String) {
        addAggregationInternal(query, function, "${escapeName(columnAlias)}.${escapeName(column.columnName)}", alias)
    }

    fun createSimpleJoinCondition(leftAlias: String, leftColumn: ColumnEntity, rightAlias: String, rightColumn: ColumnEntity): String {
        val leftPart = "${escapeName(leftAlias)}.${escapeName(leftColumn.columnName)}"
        val rightPart = "${escapeName(rightAlias)}.${escapeName(rightColumn.columnName)}"

        return "( $leftPart = $rightPart )"
    }

    fun addJoin(query: Query, strategy: Join.Strategy, table: TableEntity, alias: String, condition: String) {
        val tableName = "${escapeName(table)} AS ${escapeName(alias)}"

        when (strategy) {
            Join.Strategy.INNER_JOIN -> query.innerJoin(tableName).on(condition)
            Join.Strategy.LEFT_JOIN -> query.leftJoin(tableName).on(condition)
            Join.Strategy.RIGHT_JOIN -> query.rightJoin(tableName).on(condition)
            Join.Strategy.FULL_JOIN -> query.fullJoin(tableName).on(condition)
        }
    }

    fun createPredicate(alias: String, column: ColumnEntity, operation: Filter.Operation, value: List<String?>?): String {
        val columnPart = "${escapeName(alias)}.${escapeName(column.columnName)}"

        return when (operation) {
            Filter.Operation.IN -> createInPredicate(columnPart, value)
            Filter.Operation.NOT_IN -> createNotInPredicate(columnPart, value)
            Filter.Operation.EQUAL -> createEqualPredicate(columnPart, value?.firstOrNull())
            Filter.Operation.NOT_EQUAL -> createNotEqualPredicate(columnPart, value?.firstOrNull())
            Filter.Operation.EMPTY -> createEmpty(columnPart)
            Filter.Operation.NOT_EMPTY -> createNotEmpty(columnPart)
            Filter.Operation.GREATER_THAN -> createComparePredicate(columnPart, ">", value?.firstOrNull())
            Filter.Operation.GREATER_THAN_OR_EQUAL -> createComparePredicate(columnPart, ">=", value?.firstOrNull())
            Filter.Operation.LESS_THAN -> createComparePredicate(columnPart, "<", value?.firstOrNull())
            Filter.Operation.LESS_THAN_OR_EQUAL -> createComparePredicate(columnPart, "<=", value?.firstOrNull())
            Filter.Operation.CONTAINS -> createContainsPredicate(columnPart, value?.firstOrNull())
        }
    }

    fun setWhere(query: Query, condition: String) {
        query.where(condition)
    }

    fun addGroupBy(query: Query, alias: String, column: ColumnEntity) {
        query.groupBy("${escapeName(alias)}.${escapeName(column.columnName)}")
    }

    fun addOrderBy(query: Query, alias: String, column: ColumnEntity, direction: OrderBy.Direction) {
        addOrderByInternal(query, "${escapeName(alias)}.${escapeName(column.columnName)}", direction)
    }

    fun addOrderBy(query: Query, fieldAlias: String, direction: OrderBy.Direction) {
        addOrderByInternal(query, escapeName(fieldAlias), direction)
    }

    // Private common

    private fun escapeName(name: String): String {
        return "\"${name}\""
    }

    private fun escapeName(table: TableEntity): String {
        val tableSchema = table.tableSchema
        val tableName = table.tableName

        return if (tableSchema != null) "${escapeName(tableSchema)}.${escapeName(tableName)}" else escapeName(tableName)
    }

    private fun createStringLiteral(value: String): String {
        return "'${value.replace("'", "''")}'"
    }

    private fun addFieldInternal(query: Query, field: String) {
        val currentTable = query.currentTable
        query.table("")
        query.addField(field)
        query.table(currentTable)
    }

    private fun addAggregationInternal(query: Query, function: String, field: String, alias: String) {
        // Пока так
        val statement = when (function.lowercase()) {
            "count" -> "COUNT($field)"
            "count-distinct" -> "COUNT(DISTINCT $field)"
            "sum" -> "SUM($field)"
            "min" -> "MIN($field)"
            "max" -> "MAX($field)"
            "average" -> "AVG($field)"
            else -> throw SuiBiException("Неизвестный агрегат $function")
        }

        addFieldInternal(query, "$statement AS ${escapeName(alias)}")
    }

    private fun addOrderByInternal(query: Query, statement: String, direction: OrderBy.Direction) {
        // Защита от добавления нового значения в еманку
        val useDesc = when(direction) {
            OrderBy.Direction.ASC -> false
            OrderBy.Direction.DESC -> true
        }

        query.order(statement, useDesc)
    }

    // Private predicate

    private fun createInPredicate(columnPart: String, value: List<String?>?): String {
        if (value.isNullOrEmpty()) {
            return "FALSE"
        }

        val predicates = listOfNotNull(
            if (value.any { it == null }) "$columnPart IS NULL" else null,
            value.filterNotNull()
                .takeIf { it.isNotEmpty() }
                ?.let { v -> "$columnPart IN (${v.joinToString(", ") { createStringLiteral(it) }})" }
        )

        // Хоть 1 предикат, да будет
        return predicates.joinToString(" OR ", "( ", " )")
    }

    private fun createNotInPredicate(columnPart: String, value: List<String?>?): String {
        if (value.isNullOrEmpty()) {
            return "TRUE"
        }

        val predicates = listOfNotNull(
            if (value.any { it == null }) "$columnPart IS NOT NULL" else null,
            value.filterNotNull()
                .takeIf { it.isNotEmpty() }
                ?.let { v -> "$columnPart NOT IN (${v.joinToString(", ") { createStringLiteral(it) }})" }
        )

        // Хоть 1 предикат, да будет
        return predicates.joinToString(" AND ", "( ", " )")
    }

    private fun createEqualPredicate(columnPart: String, value: String?): String {
        return if (value == null) "$columnPart IS NULL" else "$columnPart = ${createStringLiteral(value)}"
    }

    private fun createNotEqualPredicate(columnPart: String, value: String?): String {
        return if (value == null) "$columnPart IS NOT NULL" else "$columnPart != ${createStringLiteral(value)}"
    }

    private fun createEmpty(columnPart: String): String {
        return "( $columnPart IS NULL OR ($columnPart)::TEXT = '' )"
    }

    private fun createNotEmpty(columnPart: String): String {
        return "( $columnPart IS NOT NULL AND ($columnPart)::TEXT != '' )"
    }

    private fun createComparePredicate(columnPart: String, operator: String, value: String?): String {
        if (value == null) {
            return "FALSE"
        }

        return "$columnPart $operator ${createStringLiteral(value)}"
    }

    private fun createContainsPredicate(columnPart: String, value: String?): String {
        if (value == null) {
            return "FALSE"
        }

        return "$columnPart LIKE '%${value.replace("'", "''")}%'"
    }

}