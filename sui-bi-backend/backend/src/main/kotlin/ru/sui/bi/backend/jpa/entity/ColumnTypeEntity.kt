package ru.sui.bi.backend.jpa.entity

import org.hibernate.annotations.Immutable
import ru.sui.bi.core.enumeration.ColumnType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(schema = "sui_bi", name = "column_types")
@Immutable
class ColumnTypeEntity(
    @Id
    val id: Long,
    val code: String,
    val description: String
) {

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {

        const val ID_BOOLEAN = 1L
        const val ID_INTEGER = 2L
        const val ID_DECIMAL = 3L
        const val ID_STRING = 4L
        const val ID_BINARY = 5L
        const val ID_DATE = 6L
        const val ID_TIME = 7L
        const val ID_TIME_WITH_TIMEZONE = 8L
        const val ID_TIMESTAMP = 9L
        const val ID_TIMESTAMP_WITH_TIMEZONE = 10L
        const val ID_OTHER = 11L

        fun getId(columnType: ColumnType): Long {
            return when (columnType) {
                ColumnType.BOOLEAN -> ID_BOOLEAN
                ColumnType.INTEGER -> ID_INTEGER
                ColumnType.DECIMAL -> ID_DECIMAL
                ColumnType.STRING -> ID_STRING
                ColumnType.BINARY -> ID_BINARY
                ColumnType.DATE -> ID_DATE
                ColumnType.TIME -> ID_TIME
                ColumnType.TIME_WITH_TIMEZONE -> ID_TIME_WITH_TIMEZONE
                ColumnType.TIMESTAMP -> ID_TIMESTAMP
                ColumnType.TIMESTAMP_WITH_TIMEZONE -> ID_TIMESTAMP_WITH_TIMEZONE
                ColumnType.OTHER -> ID_OTHER
            }
        }

        fun getColumnType(entity: ColumnTypeEntity): ColumnType {
            return getColumnType(entity.id)
        }

        fun getColumnType(id: Long): ColumnType {
            // Неоптимальная, то безопасная реализация
            // Если бы был when с ИДшниками, то в него могли бы забывать добавлять новое, поэтому и безопасная
            return ColumnType.values().firstOrNull { getId(it) == id } ?: error("Unknown ColumnType id = $id")
        }

    }

}