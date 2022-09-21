package ru.sui.bi.backend.core

import org.springframework.jdbc.core.namedparam.SqlParameterSource
import ru.sui.bi.backend.core.domain.TableName
import ru.sui.bi.backend.core.metaschema.ColumnMetaSchema
import ru.sui.bi.backend.core.metaschema.TableMetaSchema
import ru.sui.bi.backend.core.queryresult.QueryResult
import ru.sui.bi.backend.core.structuredquery.StructuredQuery

interface DatabaseClient : AutoCloseable {

    fun getTimeZone(): String

    fun getTableMetaSchemas(): List<TableMetaSchema>
    fun getColumnMetaSchemas(tableName: TableName): List<ColumnMetaSchema>
    fun getColumnMetaSchemas(tableNames: Iterable<TableName>): Map<TableName, List<ColumnMetaSchema>>

    fun convertToSql(structuredQuery: StructuredQuery): String

    fun testConnection() // exception при ошибке

    fun query(sql: String): QueryResult
    fun query(sql: String, paramMap: Map<String, *>): QueryResult
    fun query(sql: String, paramSource: SqlParameterSource): QueryResult

    fun query(structuredQuery: StructuredQuery) = query(convertToSql(structuredQuery))
    fun query(structuredQuery: StructuredQuery, paramMap: Map<String, *>) = query(convertToSql(structuredQuery), paramMap)
    fun query(structuredQuery: StructuredQuery, paramSource: SqlParameterSource) = query(convertToSql(structuredQuery), paramSource)

}