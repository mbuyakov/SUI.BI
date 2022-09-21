package ru.sui.bi.backend.jpa.entity

import org.hibernate.annotations.Immutable
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

    companion object {
        const val ID_BOOLEAN = 1L
        const val ID_INTEGER = 2L
        const val ID_DECIMAL = 3L
        const val ID_DATE = 4L
        const val ID_TIMESTAMP = 5L
        const val ID_TIME = 6L
        const val ID_STRING = 7L
        const val ID_BINARY = 8L
    }

}