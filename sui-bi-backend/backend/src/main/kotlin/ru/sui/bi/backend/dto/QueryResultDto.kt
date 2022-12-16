package ru.sui.bi.backend.dto

import com.fasterxml.jackson.databind.JsonNode

class QueryResultDto(
    val columns: List<String>,
    val data: List<List<JsonNode?>>
)