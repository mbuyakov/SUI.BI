package ru.sui.bi.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Информация о таблице")
data class TableDto(
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
    @field:Schema(description = "Идентификатор БД", required = true)
    val databaseId: Long,
    @field:Schema(description = "Схема", nullable = true, example = "public")
    val tableSchema: String?,
    @field:Schema(description = "Наименование", required = true, example = "example")
    val tableName: String,
    @field:Schema(description = "Тип", nullable = true, example = "BASE TABLE")
    val tableType: String?
)