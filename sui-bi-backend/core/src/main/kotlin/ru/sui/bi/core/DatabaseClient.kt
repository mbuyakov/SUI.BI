package ru.sui.bi.core

import ru.sui.bi.core.domain.TableName
import ru.sui.bi.core.metaschema.ColumnMetaSchema
import ru.sui.bi.core.metaschema.TableMetaSchema

interface DatabaseClient<Q : Query> : AutoCloseable {

    fun getTimeZone(): String

    fun getTableMetaSchemas(): List<TableMetaSchema>

    fun getColumnMetaSchemas(tableName: TableName): List<ColumnMetaSchema>
    fun getColumnMetaSchemas(tableNames: Iterable<TableName>): Map<TableName, List<ColumnMetaSchema>>

    fun createQuery(): Q

    fun convertToNativeQuery(query: Q): String

    fun testConnection() // exception при ошибке

    fun query(nativeQuery: String): QueryResult
    fun query(nativeQuery: String, paramMap: Map<String, *>): QueryResult

    fun query(query: Q): QueryResult = query(convertToNativeQuery(query))
    fun query(query: Q, paramMap: Map<String, *>): QueryResult = query(convertToNativeQuery(query), paramMap)

}