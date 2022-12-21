package ru.sui.bi.backend.dto

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Результат выполнения запроса")
class QueryResultDto(
    @field:Schema(description = "Список колонок", required = true, example = "[\"column\"]")
    val columns: List<String>,
    @field:Schema(description = "Данные", required = true, example = "[[\"value\"]]")
    val data: List<List<JsonNode?>>
)