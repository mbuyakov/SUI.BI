package ru.sui.bi.engine.postgresql

import com.querydsl.sql.Configuration
import com.querydsl.sql.PostgreSQLTemplates
import com.querydsl.sql.postgresql.PostgreSQLQuery
import com.querydsl.sql.postgresql.PostgreSQLQueryFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.RowMapperResultSetExtractor
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.jdbc.support.rowset.SqlRowSet
import ru.sui.bi.core.DatabaseClient
import ru.sui.bi.core.QueryResult
import ru.sui.bi.core.domain.TableName
import ru.sui.bi.core.enumeration.ColumnType
import ru.sui.bi.core.exception.SuiBiException
import ru.sui.bi.core.metaschema.ColumnMetaSchema
import ru.sui.bi.core.metaschema.TableMetaSchema
import java.sql.Connection
import java.sql.Types
import javax.sql.DataSource

@Suppress("SqlDialectInspection", "SqlNoDataSourceInspection")
class PostgresqlDatabaseClient(private val dataSource: DataSource) : DatabaseClient<PostgreSQLQuery<*>> {

    private val classicJdbcTemplate = JdbcTemplate(dataSource)
    private val namedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
    private val queryFactory = createQueryFactory(dataSource)

    override fun getTimeZone(): String {
        try {
            return classicJdbcTemplate.queryForObject("SHOW timezone")
        } catch (exception: Exception) {
            throw SuiBiException("Не удалось получить временную зону: ${exception.message}", exception)
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
            throw SuiBiException("Не удалось получить метаданные таблиц: ${exception.message}", exception)
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

            val typeNameToColumnTypeMapping  = getTypeNameToColumnTypeMapping()

            // Так как схема не может быть NULL, то запрос упрощен
            val query = """
                SELECT pg_namespace.nspname                                 AS table_schema,
                       pg_class.relname                                     AS table_name,
                       pg_attribute.attname                                 AS column_name,
                       CAST(CAST(pg_attribute.atttypid AS REGTYPE) AS TEXT) AS column_type_1,
                       pg_type.typname                                      AS column_type_2,
                       NOT pg_attribute.attnotnull                          AS is_nullable
                FROM pg_catalog.pg_class
                INNER JOIN pg_catalog.pg_namespace ON pg_class.relnamespace = pg_namespace.oid
                INNER JOIN pg_catalog.pg_attribute ON pg_class.oid = pg_attribute.attrelid
                INNER JOIN pg_catalog.pg_type ON pg_attribute.atttypid = pg_type.oid
                WHERE pg_class.relkind IN ('r', 'm', 'v')
                  AND NOT pg_attribute.attisdropped
                  AND pg_attribute.attnum >= 1
                  AND format('%s.%s', pg_namespace.nspname, pg_class.relname) IN (:values)
                ORDER BY pg_namespace.nspname,
                         pg_class.relname,
                         pg_attribute.attnum
            """

            val rowMapper = RowMapper<Pair<TableName, ColumnMetaSchema>> { rs, _ ->
                val tableSchema = rs.getString("table_schema")
                val tableName = rs.getString("table_name")
                val columnName = rs.getString("column_name")
                val columnType1 = rs.getString("column_type_1")
                val columnType2 = rs.getString("column_type_2")
                val isNullable = rs.getBoolean("is_nullable")

                val columnMetaSchema = ColumnMetaSchema(
                    name = columnName,
                    type = typeNameToColumnTypeMapping[columnType2] ?: ColumnType.OTHER,
                    nativeType = columnType1,
                    isNullable = isNullable
                )

                return@RowMapper TableName(tableSchema, tableName) to columnMetaSchema
            }

            val paramMap = mapOf("values" to tableNameList.map { "${it.schema}.${it.name}" })

            return namedParameterJdbcTemplate.query(query, paramMap, rowMapper)
                .groupBy { it.first }
                .mapValues { entry -> entry.value.map { it.second } }
        } catch (exception: Exception) {
            throw SuiBiException("Не удалось получить метаданные колонок: ${exception.message}", exception)
        }
    }

    override fun createQuery(): PostgreSQLQuery<*> {
        return queryFactory.query()
    }

    override fun convertToNativeQuery(query: PostgreSQLQuery<*>): String {
        try {
            return query.sql.sql
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    override fun testConnection() {
        try {
            classicJdbcTemplate.execute("SELECT 1")
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    override fun query(nativeQuery: String): QueryResult {
        return query(nativeQuery, emptyMap<String, Any?>())
    }

    override fun query(nativeQuery: String, paramMap: Map<String, *>): QueryResult {
        try {
            val rowSet = namedParameterJdbcTemplate.queryForRowSet(nativeQuery, paramMap)
            return convertSqlRowSetToQueryResult(rowSet)
        } catch (exception: SuiBiException) {
            throw exception
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    override fun close() {
        if (dataSource is AutoCloseable) {
            try {
                dataSource.close()
            } catch (exception: Exception) {
                throw SuiBiException(exception.message, exception)
            }
        }
    }

    private fun getTypeNameToColumnTypeMapping(): Map<String, ColumnType> {
        val typeNameToSqlTypeMapping = classicJdbcTemplate.execute { connection: Connection ->
            val rowMapper = RowMapper<Pair<String, Int>> { rs, _ ->
                val typeName = rs.getString("TYPE_NAME")
                val dataType = rs.getInt("DATA_TYPE")
                return@RowMapper typeName to dataType
            }

            val resultSetExtractor = RowMapperResultSetExtractor(rowMapper)

            return@execute connection.metaData.typeInfo.use { resultSetExtractor.extractData(it).toMap() }
        }

        return typeNameToSqlTypeMapping
            ?.mapValues { (typeName, sqlType) -> defineColumnType(typeName, sqlType) }
            ?: emptyMap()
    }

    private fun defineColumnType(typeName: String, sqlType: Int): ColumnType {
        return when {
            // BOOLEAN
            sqlType == Types.BIT -> ColumnType.BOOLEAN
            sqlType == Types.BOOLEAN -> ColumnType.BOOLEAN
            // INTEGER
            sqlType == Types.TINYINT -> ColumnType.INTEGER
            sqlType == Types.SMALLINT -> ColumnType.INTEGER
            sqlType == Types.INTEGER -> ColumnType.INTEGER
            sqlType == Types.BIGINT -> ColumnType.INTEGER
            // DECIMAL
            sqlType == Types.FLOAT -> ColumnType.DECIMAL
            sqlType == Types.REAL -> ColumnType.DECIMAL
            sqlType == Types.DOUBLE -> ColumnType.DECIMAL
            sqlType == Types.NUMERIC -> ColumnType.DECIMAL
            sqlType == Types.DECIMAL -> ColumnType.DECIMAL
            // BINARY
            sqlType == Types.BINARY -> ColumnType.BINARY
            sqlType == Types.VARBINARY -> ColumnType.BINARY
            sqlType == Types.LONGVARBINARY -> ColumnType.BINARY
            sqlType == Types.BLOB -> ColumnType.BINARY
            // STRING
            sqlType == Types.CHAR -> ColumnType.STRING
            sqlType == Types.NCHAR -> ColumnType.STRING
            sqlType == Types.LONGNVARCHAR -> ColumnType.STRING
            sqlType == Types.VARCHAR -> ColumnType.STRING
            sqlType == Types.NVARCHAR -> ColumnType.STRING
            sqlType == Types.LONGVARCHAR -> ColumnType.STRING
            sqlType == Types.CLOB -> ColumnType.STRING
            sqlType == Types.NCLOB -> ColumnType.STRING
            sqlType == Types.SQLXML -> ColumnType.STRING
            typeName == "uuid" -> ColumnType.STRING
            typeName == "inet" -> ColumnType.STRING
            // DATE
            sqlType == Types.DATE -> ColumnType.DATE
            // TIME
            sqlType == Types.TIME && typeName == "time" -> ColumnType.TIME
            // TIME_WITH_TIMEZONE
            sqlType == Types.TIME && typeName == "timetz" -> ColumnType.TIME_WITH_TIMEZONE
            sqlType == Types.TIME_WITH_TIMEZONE -> ColumnType.TIME_WITH_TIMEZONE
            // TIMESTAMP
            sqlType == Types.TIMESTAMP && typeName == "timestamp" -> ColumnType.TIMESTAMP
            // TIMESTAMP_WITH_TIMEZONE
            sqlType == Types.TIMESTAMP && typeName == "timestamptz" -> ColumnType.TIMESTAMP_WITH_TIMEZONE
            sqlType == Types.TIMESTAMP_WITH_TIMEZONE -> ColumnType.TIMESTAMP_WITH_TIMEZONE
            else -> ColumnType.OTHER
        }
    }

    private fun convertSqlRowSetToQueryResult(rowSet: SqlRowSet): QueryResult {
        val rowSetToQueryResultConverter = PostgresqlSqlRowSetToQueryResultConverter()
        return rowSetToQueryResultConverter.convert(rowSet)
    }

}

private fun createQueryFactory(dataSource: DataSource): PostgreSQLQueryFactory {
    val templates = PostgreSQLTemplates.builder().printSchema().build()
    val configuration = Configuration(templates).apply { useLiterals = true }

    return PostgreSQLQueryFactory(configuration) { DataSourceUtils.getConnection(dataSource) }
}