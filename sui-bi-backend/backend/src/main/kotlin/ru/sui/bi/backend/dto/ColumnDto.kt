package ru.sui.bi.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import ru.sui.bi.core.enumeration.ColumnType
import java.time.Instant

@Schema(description = "Информация о колонке")
data class ColumnDto(
    @field:Schema(description = "Идентификатор", required = true)
    val id: Long,
    @field:Schema(description = "Дата и время создания", required = true, example = "2023-01-01T00:00:00.000Z")
    val created: Instant,
    @field:Schema(description = "Идентификатор создателя", nullable = true)
    val creatorId: Long?,
    @field:Schema(description = "Дата и время последнего изменения", required = true, example = "2023-01-01T00:00:00.000Z")
    val lastModified: Instant,
    @field:Schema(description = "Идентификатор последнего изменившего", nullable = true)
    val lastModifierId: Long?,
    @field:Schema(description = "Идентификатор таблицы", required = true)
    val tableId: Long,
    @field:Schema(description = "Наименование", required = true, example = "id")
    val columnName: String,
    @field:Schema(description = "Тип", required = true, example = "INTEGER")
    val columnType: ColumnType,
    @field:Schema(description = "Сырой тип", required = true, example = "bigint")
    val rawColumnType: String,
    @field:Schema(description = "Значение может отсутствовать", required = true)
    val nullable: Boolean
)