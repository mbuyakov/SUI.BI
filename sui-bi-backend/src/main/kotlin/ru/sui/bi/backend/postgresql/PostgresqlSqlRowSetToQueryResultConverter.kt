package ru.sui.bi.backend.postgresql

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.jdbc.support.rowset.SqlRowSet
import ru.sui.bi.backend.core.queryresult.ColumnValue
import ru.sui.bi.backend.core.queryresult.QueryResult
import ru.sui.bi.backend.core.queryresult.impl.*
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Blob
import java.sql.Clob
import java.sql.SQLXML
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.ArrayList

private val DEFAULT_ZONE_OFFSET = OffsetDateTime.now().offset

class PostgresqlSqlRowSetToQueryResultConverter {

    fun convert(sqlRowSet: SqlRowSet): QueryResult {
        return QueryResult(
            columnNames = sqlRowSet.metaData.columnNames.toList(),
            rows = createRowSequence(sqlRowSet)
        )
    }

    private fun createRowSequence(sqlRowSet: SqlRowSet): Sequence<List<ColumnValue<out Any, out JsonNode>?>> {
        val columnCount = sqlRowSet.metaData.columnCount

        return sequence {
            while (sqlRowSet.next()) {
                val row = ArrayList<ColumnValue<out Any, out JsonNode>?>(columnCount)

                (1..columnCount).forEach { columnIndex ->
                    val rawColumnValue = sqlRowSet.getObject(columnIndex)
                    val rawColumnTypeName = sqlRowSet.metaData.getColumnTypeName(columnIndex)
                    val columnValue = convertRawColumnValueToColumnValue(rawColumnValue, rawColumnTypeName)
                    row.add(columnValue)
                }

                yield(row)
            }
        }
    }

    private fun convertRawColumnValueToColumnValue(rawColumnValue: Any?, rawColumnTypeName: String): ColumnValue<out Any, out JsonNode>? {
        // Все возможные типы ищи в PgResultSet
        // Временно умышленно пропустил: PgArray, PgObject, hstore
        return when (rawColumnValue) {
            null -> null
            is Boolean -> BooleanColumnValue(rawColumnValue)
            is Byte -> ByteColumnValue(rawColumnValue)
            is Char -> CharColumnValue(rawColumnValue)
            is Short -> ShortColumnValue(rawColumnValue)
            is Int -> IntColumnValue(rawColumnValue)
            is Long -> LongColumnValue(rawColumnValue)
            is Float -> FloatColumnValue(rawColumnValue)
            is Double -> DoubleColumnValue(rawColumnValue)
            is BigInteger -> BigIntegerColumnValue(rawColumnValue)
            is BigDecimal -> BigDecimalColumnValue(rawColumnValue)
            is java.sql.Date -> LocalDateColumnValue(rawColumnValue.toLocalDate())
            is java.sql.Timestamp -> convertTimestampToColumnValue(rawColumnValue, rawColumnTypeName)
            is java.sql.Time -> convertTimeToColumnValue(rawColumnValue, rawColumnTypeName)
            is ByteArray -> ByteArrayColumnValue(rawColumnValue)
            is Blob -> ByteArrayColumnValue(rawColumnValue.binaryStream.use { it.readBytes() })
            is String -> StringColumnValue(rawColumnValue)
            is Clob -> StringColumnValue(rawColumnValue.characterStream.use { it.readText() })
            is UUID -> UuidColumnValue(rawColumnValue)
            is SQLXML -> XmlColumnValue(rawColumnValue.string)
            else -> UnknownColumnValue(rawColumnValue)
        }
    }

    private fun convertTimestampToColumnValue(timestamp: java.sql.Timestamp, rawColumnTypeName: String): ColumnValue<out Any, out JsonNode> {
        return if (rawColumnTypeName == "timestamptz") {
            OffsetDateTimeColumnValue(timestamp.toLocalDateTime().atOffset(DEFAULT_ZONE_OFFSET))
        } else {
            LocalDateTimeColumnValue(timestamp.toLocalDateTime())
        }
    }

    private fun convertTimeToColumnValue(time: java.sql.Time, rawColumnTypeName: String): ColumnValue<out Any, out JsonNode> {
        return if (rawColumnTypeName == "timetz") {
            OffsetTimeColumnValue(time.toLocalTime().atOffset(DEFAULT_ZONE_OFFSET))
        } else {
            LocalTimeColumnValue(time.toLocalTime())
        }
    }

}