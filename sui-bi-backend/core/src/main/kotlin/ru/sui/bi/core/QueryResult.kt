package ru.sui.bi.core

import com.fasterxml.jackson.databind.JsonNode
import ru.sui.bi.core.columnvalue.ColumnValue

class QueryResult(
    val columnNames: List<String>,
    val rows: Sequence<List<ColumnValue<out Any, out JsonNode>?>>
)