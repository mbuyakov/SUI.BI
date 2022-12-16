package ru.sui.bi.backend.structuredquery.engine

import com.querydsl.sql.ColumnMetadata
import com.querydsl.sql.RelationalPathBase
import ru.sui.bi.backend.jpa.entity.ColumnEntity
import ru.sui.bi.backend.jpa.entity.ColumnTypeEntity
import ru.sui.bi.backend.jpa.entity.TableEntity
import ru.sui.bi.core.enumeration.ColumnType
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Types
import java.time.*

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class MetaRelationalPath(
    val alias: String,
    val metaTable: TableEntity,
    val metaColumns: List<ColumnEntity>
) : RelationalPathBase<Any>(Any::class.java, alias, metaTable.tableSchema, metaTable.tableName) {

    init {
        metaColumns.forEachIndexed { index, column ->
            val columnName = column.columnName
            val columnType = ColumnTypeEntity.getColumnType(column.columnType)
            val columnIsNullable = column.isNullable

            val (path, type) = when (columnType) {
                ColumnType.BOOLEAN -> createBoolean(columnName) to Types.BOOLEAN
                ColumnType.INTEGER -> createNumber(columnName, BigInteger::class.java) to Types.BIGINT
                ColumnType.DECIMAL -> createNumber(columnName, BigDecimal::class.java) to Types.DECIMAL
                ColumnType.STRING -> createString(columnName) to Types.VARCHAR
                ColumnType.BINARY -> createSimple(columnName, ByteArray::class.java) to Types.BINARY
                ColumnType.DATE -> createDate(columnName, LocalDate::class.java) to Types.DATE
                ColumnType.TIME -> createTime(columnName, LocalTime::class.java) to Types.TIME
                ColumnType.TIME_WITH_TIMEZONE -> createTime(columnName, OffsetTime::class.java) to Types.TIME
                ColumnType.TIMESTAMP -> createDateTime(columnName, LocalDateTime::class.java) to Types.TIMESTAMP
                ColumnType.TIMESTAMP_WITH_TIMEZONE -> createDateTime(columnName, OffsetDateTime::class.java) to Types.TIMESTAMP_WITH_TIMEZONE
                ColumnType.OTHER -> createSimple(columnName, Any::class.java) to Types.OTHER
            }

            val metadata = ColumnMetadata.named(columnName)
                .withIndex(index + 1)
                .ofType(type)
                .let { if (columnIsNullable) it else it.notNull() }

            this.addMetadata(path, metadata)
        }
    }

}