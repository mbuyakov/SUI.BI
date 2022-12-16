package ru.sui.bi.backend.structuredquery.engine

import com.querydsl.core.types.*
import com.querydsl.core.types.dsl.DslExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.Wildcard
import org.springframework.data.repository.findByIdOrNull
import ru.sui.bi.backend.jpa.entity.ColumnEntity
import ru.sui.bi.backend.jpa.entity.TableEntity
import ru.sui.bi.backend.jpa.repository.ColumnRepository
import ru.sui.bi.backend.jpa.repository.TableRepository
import ru.sui.bi.backend.structuredquery.domain.*
import ru.sui.bi.core.DatabaseClient
import ru.sui.bi.core.Query
import ru.sui.bi.core.exception.SuiBiException

@Suppress("UNCHECKED_CAST")
class DefaultStructuredQueryProcessEngine(
    private val tableRepository: TableRepository,
    private val columnRepository: ColumnRepository
) : StructuredQueryProcessEngine {

    override fun process(client: DatabaseClient<out Query>, structuredQuery: StructuredQuery): Query {
        // Достаем всякое
        val sourceTableId = structuredQuery.query.sourceTable
        val fields = structuredQuery.query.fields
        val joins = structuredQuery.query.joins
        val aggregation = structuredQuery.query.aggregation
        val groupBy = structuredQuery.query.groupBy
        val filter = structuredQuery.query.filter
        val orderBy = structuredQuery.query.orderBy
        val limit = structuredQuery.query.limit
        val offset = structuredQuery.query.offset

        // Определяем sourceTable
        val sourceTable = getTable(sourceTableId)
        val sourceTableAlias = sourceTable.tableName

        // Создаем и наполняем metaHelper
        val metaHelper = MetaHelper()
        metaHelper.addTable(sourceTableAlias, sourceTable, getColumns(sourceTable))
        joins?.forEach { metaHelper.addTable(it.alias, getTable(it.sourceTable), getColumns(it.sourceTable)) }

        // Создаем запрос
        val clientQuery = client.createQuery()

        // Объявляем selection
        val selection = mutableListOf<Expression<*>>()

        // Fields
        fields?.forEach {
            val alias = it.joinAlias ?: sourceTableAlias
            val column = metaHelper.getColumnPath(alias, it.field)
            selection.add(column)
        }

        // FROM
        clientQuery.from(metaHelper.getTablePath(sourceTableAlias))

        // JOINS
        joins?.forEach { join ->
            val joinTable = metaHelper.getTablePath(join.alias)
            val joinLeftAlias = join.leftOn.alias ?: sourceTableAlias
            val joinLeftColumn = metaHelper.getColumnPath(joinLeftAlias, join.leftOn.field) as Path<Any?>
            val joinRightAlias = join.rightOn.alias ?: sourceTableAlias
            val joinRightColumn = metaHelper.getColumnPath(joinRightAlias, join.rightOn.field) as Path<Any?>
            val joinStrategy = join.strategy ?: Join.Strategy.LEFT_JOIN

            clientQuery.join(joinTable, joinStrategy).on(ExpressionUtils.eq(joinLeftColumn, joinRightColumn))
        }

        // WHERE
        if (filter != null) {
            val predicate = convertFilterTreeToPredicate(filter) {
                val alias = it.joinAlias ?: sourceTableAlias
                val column = metaHelper.getColumnPath(alias, it.field)

                return@convertFilterTreeToPredicate convertFilterToPredicate(
                    column = column,
                    operation = it.operation,
                    value = it.value
                )
            }

            clientQuery.where(predicate)
        }

        // GROUP BY
        if (groupBy != null) {
            val groupByExpressions = groupBy.map {
                val alias = it.joinAlias ?: sourceTableAlias
                return@map metaHelper.getColumnPath(alias, it.field)
            }

            selection.addAll(groupByExpressions)
            clientQuery.groupBy(*groupByExpressions.toTypedArray())
        }

        // Aggregation
        aggregation?.forEach {
            if (it.field == null) {
                val agg = createAggregation(it.aggFunction, null, it.fieldAlias)
                selection.add(agg)
            } else {
                val alias = it.joinAlias ?: sourceTableAlias
                val column = metaHelper.getColumnPath(alias, it.field)
                val agg = createAggregation(it.aggFunction, column, it.fieldAlias)
                selection.add(agg)
            }
        }

        // Устанавливаем selection
        if (selection.isNotEmpty()) {
            clientQuery.select(*selection.toTypedArray())
        } else {
            clientQuery.select(*metaHelper.getTablePaths().flatMap { it.columns }.toTypedArray())
        }

        // ORDER BY
        if (!orderBy.isNullOrEmpty()) {
            val orderSpecifiers = orderBy.map {
                val order = when (it.order) {
                    OrderBy.Direction.ASC -> Order.ASC
                    OrderBy.Direction.DESC -> Order.DESC
                    else -> Order.ASC
                }

                val target = if (it.fieldAlias != null) {
                    ExpressionUtils.path(Comparable::class.java, it.fieldAlias)
                } else {
                    metaHelper.getColumnPath(it.joinAlias ?: sourceTableAlias, it.field!!)
                }

                return@map OrderSpecifier(order, target as Expression<Comparable<*>>)
            }

            clientQuery.orderBy(*orderSpecifiers.toTypedArray())
        }

        // LIMIT
        if (limit != null) {
            clientQuery.limit(limit.toLong())
        }

        // OFFSET
        if (offset != null) {
            clientQuery.offset(offset.toLong())
        }

        return clientQuery
    }

    private fun getTable(id: Long): TableEntity {
        return tableRepository.findByIdOrNull(id) ?: throw SuiBiException("Не удалось найти таблицу с ИД = $id")
    }

    private fun getColumns(tableId: Long): List<ColumnEntity> {
        return columnRepository.findAllByTable(tableRepository.getReferenceById(tableId))
    }

    private fun getColumns(table: TableEntity): List<ColumnEntity> {
        return columnRepository.findAllByTable(table)
    }

    private fun <T : Query> T.join(target: EntityPath<*>, strategy: Join.Strategy): T {
        return when (strategy) {
            Join.Strategy.INNER_JOIN -> innerJoin(target) as T
            Join.Strategy.LEFT_JOIN -> leftJoin(target) as T
            Join.Strategy.RIGHT_JOIN -> rightJoin(target) as T
            Join.Strategy.FULL_JOIN -> fullJoin(target) as T
        }
    }

    private fun createAggregation(function: String, column: Path<*>?, alias: String): Expression<*> {
        val expression: DslExpression<*>

        if (column == null) {
            expression = when (function.lowercase()) {
                "count" -> Wildcard.count
                "count-distinct" -> Wildcard.countDistinct
                else -> throw SuiBiException("Неизвестный агрегат для wildcard «$function»")
            }
        } else {
            expression = when (function.lowercase()) {
                "count" -> Expressions.operation(Long::class.java, Ops.AggOps.COUNT_ALL_AGG, column)
                "count-distinct" -> Expressions.operation(Long::class.java, Ops.AggOps.COUNT_DISTINCT_ALL_AGG, column)
                "sum" -> Expressions.operation(column.type, Ops.AggOps.SUM_AGG, column)
                "min" -> Expressions.operation(column.type, Ops.AggOps.MIN_AGG, column)
                "max" -> Expressions.operation(column.type, Ops.AggOps.MAX_AGG, column)
                "average" -> Expressions.operation(column.type, Ops.AggOps.AVG_AGG, column)
                else -> throw SuiBiException("Неизвестный агрегат «$function»")
            }
        }

        return expression.`as`(alias)
    }

    private fun convertFilterTreeToPredicate(filterTree: PredicateTree<Filter>, predicateCreator: (Filter) -> Predicate): Predicate {
        val subPredicates = filterTree.nodes.map {
            when (it) {
                is PredicateTree.Node.SubTree -> convertFilterTreeToPredicate(it.tree, predicateCreator)
                is PredicateTree.Node.Value -> predicateCreator(it.value)
            }
        }

        if (subPredicates.isEmpty()) {
            return Expressions.TRUE
        }

        var result = when (filterTree.predicate) {
            PredicateTree.Predicate.AND -> ExpressionUtils.allOf(*subPredicates.toTypedArray())!!
            PredicateTree.Predicate.OR -> ExpressionUtils.anyOf(*subPredicates.toTypedArray())!!
        }

        if (filterTree.not == true) {
            result = result.not()
        }

        return result
    }

    private fun convertFilterToPredicate(column: Path<*>, operation: Filter.Operation, value: List<String?>?): Predicate {
        return when (operation) {
            Filter.Operation.IN -> createInPredicate(column, value)
            Filter.Operation.NOT_IN -> createInPredicate(column, value).not()
            Filter.Operation.EQUAL -> createEqPredicate(column, value?.firstOrNull())
            Filter.Operation.NOT_EQUAL -> createEqPredicate(column, value?.firstOrNull()).not()
            Filter.Operation.EMPTY -> createEmptyPredicate(column)
            Filter.Operation.NOT_EMPTY -> createEmptyPredicate(column).not()
            Filter.Operation.GREATER_THAN -> createComparePredicate(column, Ops.GT, value?.firstOrNull())
            Filter.Operation.GREATER_THAN_OR_EQUAL -> createComparePredicate(column, Ops.GOE, value?.firstOrNull())
            Filter.Operation.LESS_THAN -> createComparePredicate(column, Ops.LT, value?.firstOrNull())
            Filter.Operation.LESS_THAN_OR_EQUAL -> createComparePredicate(column, Ops.LOE, value?.firstOrNull())
            Filter.Operation.CONTAINS -> createContainsPredicate(column, value?.firstOrNull())
        }
    }

    private fun createInPredicate(column: Path<*>, value: List<String?>?): Predicate {
        if (value.isNullOrEmpty()) {
            return Expressions.FALSE
        }

        return ExpressionUtils.`in`(column as Path<Any>, value)
    }

    private fun createEqPredicate(column: Path<*>, value: String?): Predicate {
        if (value == null) {
            return ExpressionUtils.isNull(column)
        }

        return ExpressionUtils.eqConst(column as Path<Any>, value)
    }

    private fun createEmptyPredicate(column: Path<*>): Predicate {
        val result = ExpressionUtils.anyOf(
            ExpressionUtils.isNull(column),
            ExpressionUtils.eqConst(Expressions.stringOperation(Ops.STRING_CAST, column), "")
        )

        return result!!
    }

    private fun createComparePredicate(column: Path<*>, ops: Ops, value: String?): Predicate {
        if (value == null) {
            return Expressions.FALSE
        }

        return Expressions.booleanOperation(ops, column, Expressions.constant(value))
    }

    private fun createContainsPredicate(column: Path<*>, value: String?): Predicate {
        if (value == null) {
            return Expressions.FALSE
        }

        return Expressions.booleanOperation(Ops.STRING_CONTAINS_IC, column, Expressions.constant(value))
    }

}