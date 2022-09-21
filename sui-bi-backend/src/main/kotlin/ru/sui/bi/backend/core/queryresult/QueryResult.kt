package ru.sui.bi.backend.core.queryresult

import com.fasterxml.jackson.databind.JsonNode

class QueryResult(
    val columnNames: List<String>,
    val rows: Sequence<List<ColumnValue<out Any, out JsonNode>?>>
)