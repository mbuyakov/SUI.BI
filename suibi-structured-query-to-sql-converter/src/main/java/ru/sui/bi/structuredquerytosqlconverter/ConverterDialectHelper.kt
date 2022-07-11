package ru.sui.bi.structuredquerytosqlconverter

import io.zeko.db.sql.Query
import ru.sui.bi.structuredquerytosqlconverter.model.Filter
import ru.sui.bi.structuredquerytosqlconverter.model.Join
import ru.sui.bi.structuredquerytosqlconverter.model.OrderBy
import ru.sui.suientity.entity.suimeta.ColumnInfo
import ru.sui.suientity.entity.suimeta.TableInfo

interface ConverterDialectHelper {

    fun getType(): String

    fun createQuery(): Query

    fun setFrom(query: Query, table: TableInfo)

    fun selectAll(query: Query)

    fun selectField(query: Query, alias: String, column: ColumnInfo)

    fun addAggregation(query: Query, function: String, alias: String)

    fun addAggregation(query: Query, function: String, columnAlias: String, column: ColumnInfo, alias: String)

    fun createSimpleJoinCondition(leftAlias: String, leftColumn: ColumnInfo, rightAlias: String, rightColumn: ColumnInfo): String

    fun addJoin(query: Query, strategy: Join.Strategy, table: TableInfo, alias: String, condition: String)

    fun createPredicate(alias: String, column: ColumnInfo, operation: Filter.Operation, value: List<String?>?): String

    fun setWhere(query: Query, condition: String)

    fun addGroupBy(query: Query, alias: String, column: ColumnInfo)

    fun addOrderBy(query: Query, alias: String, column: ColumnInfo, direction: OrderBy.Direction)

    fun addOrderBy(query: Query, fieldAlias: String, direction: OrderBy.Direction)

}