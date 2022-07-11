package ru.sui.bi.structuredquerytosqlconverter.dialect

import io.zeko.db.sql.Query
import ru.sui.bi.structuredquerytosqlconverter.ConverterDialectHelper
import ru.sui.bi.structuredquerytosqlconverter.exception.ConversionException
import ru.sui.bi.structuredquerytosqlconverter.extension.addField
import ru.sui.bi.structuredquerytosqlconverter.extension.currentTable
import ru.sui.bi.structuredquerytosqlconverter.extension.fullJoin
import ru.sui.bi.structuredquerytosqlconverter.model.Filter
import ru.sui.bi.structuredquerytosqlconverter.model.Join
import ru.sui.bi.structuredquerytosqlconverter.model.OrderBy
import ru.sui.suientity.entity.suimeta.ColumnInfo
import ru.sui.suientity.entity.suimeta.TableInfo

class PostgresConverterDialectHelper : ConverterDialectHelper {

    override fun getType(): String {
        return "PostgreSQL"
    }

    override fun createQuery(): Query {
        // Отключаем багованное экранирование и странный функционал AS, делаем все руками
        return Query(espChar = "`~`~`~`~`~`", asChar = "`~`~`~`~`~`", espTableName = false)
    }

    override fun setFrom(query: Query, table: TableInfo) {
        query.from("${escapeName(table.schemaName)}.${escapeName(table.tableName)}")
    }

    override fun selectAll(query: Query) {
        query.fields("*")
    }

    override fun selectField(query: Query, alias: String, column: ColumnInfo) {
        addFieldInternal(query, "${escapeName(alias)}.${escapeName(column.columnName)} AS ${escapeName(column.columnName)}")
    }

    override fun addAggregation(query: Query, function: String, alias: String) {
        addAggregationInternal(query, function, "*", alias)
    }

    override fun addAggregation(query: Query, function: String, columnAlias: String, column: ColumnInfo, alias: String) {
        addAggregationInternal(query, function, "${escapeName(columnAlias)}.${escapeName(column.columnName)}", alias)
    }

    override fun createSimpleJoinCondition(leftAlias: String, leftColumn: ColumnInfo, rightAlias: String, rightColumn: ColumnInfo): String {
        val leftPart = "${escapeName(leftAlias)}.${escapeName(leftColumn.columnName)}"
        val rightPart = "${escapeName(rightAlias)}.${escapeName(rightColumn.columnName)}"

        return "( $leftPart = $rightPart )"
    }

    override fun addJoin(query: Query, strategy: Join.Strategy, table: TableInfo, alias: String, condition: String) {
        val tableName = "${escapeName(table.schemaName)}.${escapeName(table.tableName)} AS ${escapeName(alias)}"

        when (strategy) {
            Join.Strategy.INNER_JOIN -> query.innerJoin(tableName).on(condition)
            Join.Strategy.LEFT_JOIN -> query.leftJoin(tableName).on(condition)
            Join.Strategy.RIGHT_JOIN -> query.rightJoin(tableName).on(condition)
            Join.Strategy.FULL_JOIN -> query.fullJoin(tableName).on(condition)
        }
    }

    override fun createPredicate(alias: String, column: ColumnInfo, operation: Filter.Operation, value: List<String?>?): String {
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

    override fun setWhere(query: Query, condition: String) {
        query.where(condition)
    }

    override fun addGroupBy(query: Query, alias: String, column: ColumnInfo) {
        query.groupBy("${escapeName(alias)}.${escapeName(column.columnName)}")
    }

    override fun addOrderBy(query: Query, alias: String, column: ColumnInfo, direction: OrderBy.Direction) {
        addOrderByInternal(query, "${escapeName(alias)}.${escapeName(column.columnName)}", direction)
    }

    override fun addOrderBy(query: Query, fieldAlias: String, direction: OrderBy.Direction) {
        addOrderByInternal(query, escapeName(fieldAlias), direction)
    }

    // Private common

    private fun escapeName(name: String): String {
        return "\"${name}\""
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
        val statement = when (function.toLowerCase()) {
            "count" -> "COUNT($field)"
            "count-distinct" -> "COUNT(DISTINCT $field)"
            "sum" -> "SUM($field)"
            "min" -> "MIN($field)"
            "max" -> "MAX($field)"
            "average" -> "AVG($field)"
            else -> throw ConversionException("Неизвестный агрегат $function")
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