package ru.sui.bi.engine.postgresql

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.jdbc.support.rowset.SqlRowSet
import ru.sui.bi.core.exception.SuiBiException
import ru.sui.bi.core.columnvalue.ColumnValue
import ru.sui.bi.core.QueryResult
import ru.sui.bi.core.columnvalue.impl.*
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Blob
import java.sql.Clob
import java.sql.SQLXML
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList

class PostgresqlSqlRowSetToQueryResultConverter(private val params: Params = Params()) {

    class Params(val zoneOffset: ZoneOffset = OffsetDateTime.now().offset)

    fun convert(rowSet: SqlRowSet): QueryResult {
        try {
            return QueryResult(
                columnNames = rowSet.metaData.columnNames.toList(),
                rows = createRowSequence(rowSet)
            )
        } catch (exception: SuiBiException) {
            throw exception
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    private fun createRowSequence(sqlRowSet: SqlRowSet): Sequence<List<ColumnValue<out Any, out JsonNode>?>> {
        return sequence {
            try {
                val columnCount = sqlRowSet.metaData.columnCount

                while (sqlRowSet.next()) {
                    val row = ArrayList<ColumnValue<out Any, out JsonNode>?>(columnCount)

                    (1..columnCount).forEach { columnIndex ->
                        val rawColumnValue = sqlRowSet.getObject(columnIndex)
                        val nativeColumnTypeName = sqlRowSet.metaData.getColumnTypeName(columnIndex)
                        val columnValue = convertRawColumnValueToColumnValue(rawColumnValue, nativeColumnTypeName)
                        row.add(columnValue)
                    }

                    yield(row)
                }
            } catch (exception: Exception) {
                throw SuiBiException(exception.message, exception)
            }
        }
    }

    private fun convertRawColumnValueToColumnValue(rawColumnValue: Any?, nativeColumnTypeName: String): ColumnValue<out Any, out JsonNode>? {
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
            is java.sql.Timestamp -> convertTimestampToColumnValue(rawColumnValue, nativeColumnTypeName)
            is java.sql.Time -> convertTimeToColumnValue(rawColumnValue, nativeColumnTypeName)
            is ByteArray -> ByteArrayColumnValue(rawColumnValue)
            is Blob -> ByteArrayColumnValue(rawColumnValue.binaryStream.use { it.readBytes() })
            is String -> StringColumnValue(rawColumnValue)
            is Clob -> StringColumnValue(rawColumnValue.characterStream.use { it.readText() })
            is UUID -> UuidColumnValue(rawColumnValue)
            is SQLXML -> XmlColumnValue(rawColumnValue.string)
            else -> UnknownColumnValue(rawColumnValue)
        }
    }

    private fun convertTimestampToColumnValue(timestamp: java.sql.Timestamp, nativeColumnTypeName: String): ColumnValue<out Any, out JsonNode> {
        return if (nativeColumnTypeName == "timestamptz") {
            OffsetDateTimeColumnValue(timestamp.toLocalDateTime().atOffset(params.zoneOffset))
        } else {
            LocalDateTimeColumnValue(timestamp.toLocalDateTime())
        }
    }

    private fun convertTimeToColumnValue(time: java.sql.Time, nativeColumnTypeName: String): ColumnValue<out Any, out JsonNode> {
        return if (nativeColumnTypeName == "timetz") {
            OffsetTimeColumnValue(time.toLocalTime().atOffset(params.zoneOffset))
        } else {
            LocalTimeColumnValue(time.toLocalTime())
        }
    }

}