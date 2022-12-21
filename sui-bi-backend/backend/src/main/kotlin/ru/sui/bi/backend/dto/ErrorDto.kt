package ru.sui.bi.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Информация об ошибке")
data class ErrorDto(
    @field:Schema(description = "Дата и время возникновения", required = true, example = "2023-01-01T00:00:00.000Z")
    val timestamp: Instant,
    @field:Schema(description = "Статус", required = true, example = "Bad Request")
    val status: String,
    @field:Schema(description = "Код статуса", required = true, example = "400")
    val statusCode: Int,
    @field:Schema(description = "Сообщение", required = true, example = "Example error message")
    val message: String,
    @field:Schema(description = "Детальная информация", required = true, example = "org.example.Exception: Error message")
    val details: String
)