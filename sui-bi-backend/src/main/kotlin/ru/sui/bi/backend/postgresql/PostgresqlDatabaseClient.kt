package ru.sui.bi.backend.postgresql

import com.zaxxer.hikari.HikariDataSource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.queryForObject
import org.springframework.jdbc.support.rowset.SqlRowSet
import ru.sui.bi.backend.core.DatabaseClient
import ru.sui.bi.backend.core.domain.TableName
import ru.sui.bi.backend.core.enumeration.ColumnType
import ru.sui.bi.backend.core.exception.SuiBiException
import ru.sui.bi.backend.core.metaschema.ColumnMetaSchema
import ru.sui.bi.backend.core.metaschema.TableMetaSchema
import ru.sui.bi.backend.core.queryresult.QueryResult
import ru.sui.bi.backend.core.structuredquery.StructuredQuery
import ru.sui.bi.backend.jpa.repository.ColumnRepository
import ru.sui.bi.backend.jpa.repository.TableRepository

@Suppress("SqlDialectInspection", "SqlNoDataSourceInspection")
class PostgresqlDatabaseClient(
    private val dataSource: HikariDataSource,
    // Костыли, т.к. StructuredQuery, передаваемый в клиенты содержит ИДшники (в будущем он будет раскрываться до передачи)
    tableRepository: TableRepository,
    columnRepository: ColumnRepository
) : DatabaseClient {

    private val classicJdbcTemplate = JdbcTemplate(dataSource)
    private val namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
    private val sqlRowSetToQueryResultConverter = PostgresqlSqlRowSetToQueryResultConverter()
    private val structuredQueryToSqlConverter = PostgresqlStructuredQueryToSqlConverter(tableRepository, columnRepository)

    override fun getTimeZone(): String {
        try {
            return classicJdbcTemplate.queryForObject("SHOW timezone")
        } catch (exception: Exception) {
            throw SuiBiException("Не удалось получить таймзону", exception)
        }
    }

    override fun getTableMetaSchemas(): List<TableMetaSchema> {
        try {
            val query = """
            SELECT pg_namespace.nspname AS table_schema,
                   pg_class.relname     AS table_name,
                   CASE
                       WHEN relkind = 'r' THEN 'BASE TABLE'
                       WHEN relkind = 'v' THEN 'VIEW'
                       WHEN relkind = 'm' THEN 'MATERIALIZED VIEW'
                       END              AS table_type
            FROM pg_catalog.pg_class
            INNER JOIN pg_catalog.pg_namespace ON pg_class.relnamespace = pg_namespace.oid
            WHERE pg_class.relkind IN ('r', 'm', 'v')
        """

            val rowMapper = RowMapper<TableMetaSchema> { rs, _ ->
                TableMetaSchema(
                    schema = rs.getString("table_schema"),
                    name = rs.getString("table_name"),
                    type = rs.getString("table_type")
                )
            }

            return classicJdbcTemplate.query(query, rowMapper)
        } catch (exception: Exception) {
            throw SuiBiException("Не удалось получить метаданные таблиц", exception)
        }
    }

    override fun getColumnMetaSchemas(tableName: TableName): List<ColumnMetaSchema> {
        return getColumnMetaSchemas(listOf(tableName)).values.firstOrNull() ?: emptyList()
    }

    override fun getColumnMetaSchemas(tableNames: Iterable<TableName>): Map<TableName, List<ColumnMetaSchema>> {
        try {
            val tableNameList = tableNames.toList()

            if (tableNameList.isEmpty()) {
                return emptyMap()
            }

            // Так как схема не может быть NULL, то запрос упрощен
            val query = """
            SELECT pg_namespace.nspname                                 AS table_schema,
                   pg_class.relname                                     AS table_name,
                   pg_attribute.attname                                 AS column_name,
                   CAST(CAST(pg_attribute.atttypid AS REGTYPE) AS TEXT) AS column_type,
                   NOT pg_attribute.attnotnull                          AS is_nullable
            FROM pg_catalog.pg_class
            INNER JOIN pg_catalog.pg_namespace ON pg_class.relnamespace = pg_namespace.oid
            INNER JOIN pg_catalog.pg_attribute ON pg_class.oid = pg_attribute.attrelid
            WHERE pg_class.relkind IN ('r', 'm', 'v')
              AND NOT pg_attribute.attisdropped
              AND pg_attribute.attnum >= 1
              AND format('%s.%s', pg_namespace.nspname, pg_class.relname) IN (:values)
            ORDER BY pg_namespace.nspname,
                     pg_class.relname,
                     pg_attribute.attnum
        """

            val rowMapper = RowMapper<Pair<TableName, ColumnMetaSchema>> { rs, _ ->
                val tableName = TableName(
                    schema = rs.getString("table_schema"),
                    name = rs.getString("table_schema")
                )

                val rawColumnType = rs.getString("column_type")

                val columnMetaSchema = ColumnMetaSchema(
                    name = rs.getString("column_name"),
                    type = defineColumnTypeByRawColumnType(rawColumnType),
                    rawType = rawColumnType,
                    isNullable = rs.getBoolean("is_nullable")
                )

                return@RowMapper tableName to columnMetaSchema
            }

            val paramMap = mapOf("values" to tableNameList.map { "${it.schema}.${it.name}" })

            return namedParameterJdbcTemplate.query(query, paramMap, rowMapper)
                .groupBy { it.first }
                .mapValues { entry -> entry.value.map { it.second } }
        } catch (exception: Exception) {
            throw SuiBiException("Не удалось получить метаданные колонок", exception)
        }
    }

    override fun convertToSql(structuredQuery: StructuredQuery): String {
        return structuredQueryToSqlConverter.convert(structuredQuery)
    }

    override fun testConnection() {
        try {
            classicJdbcTemplate.execute("SELECT 1")
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    override fun query(sql: String): QueryResult {
        try {
            val sqlRowSet = classicJdbcTemplate.queryForRowSet(sql)
            return convertSqlRowSetToQueryResult(sqlRowSet)
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    override fun query(sql: String, paramMap: Map<String, *>): QueryResult {
        try {
            val sqlRowSet = namedParameterJdbcTemplate.queryForRowSet(sql, paramMap)
            return convertSqlRowSetToQueryResult(sqlRowSet)
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    override fun query(sql: String, paramSource: SqlParameterSource): QueryResult {
        try {
            val sqlRowSet = namedParameterJdbcTemplate.queryForRowSet(sql, paramSource)
            return convertSqlRowSetToQueryResult(sqlRowSet)
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    override fun close() {
        try {
            dataSource.close()
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    // TODO: Перенести в отдельный класс?
    private fun defineColumnTypeByRawColumnType(rawColumnType: String): ColumnType {
        // TODO: Перенести в БД?
        return when (rawColumnType.lowercase()) {
            "int2", "int4", "int8", "smallint", "int", "integer", "bigint", "smallserial", "serial", "bigserial" -> ColumnType.INTEGER
            "decimal", "numeric", "real", "float4", "float8", "double precision", "money" -> ColumnType.DECIMAL
            "tinyint", "boolean" -> ColumnType.BOOLEAN
            "bytea" -> ColumnType.BINARY
            "date" -> ColumnType.DATE
            "timestamp", "timestamp without time zone", "timestamp with time zone" -> ColumnType.TIMESTAMP
            "time", "time without time zone", "time with time zone" -> ColumnType.TIME
            else -> ColumnType.STRING
        }
    }

    private fun convertSqlRowSetToQueryResult(sqlRowSet: SqlRowSet): QueryResult {
        return sqlRowSetToQueryResultConverter.convert(sqlRowSet)
    }

}