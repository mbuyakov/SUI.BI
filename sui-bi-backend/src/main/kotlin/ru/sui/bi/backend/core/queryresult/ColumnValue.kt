package ru.sui.bi.backend.core.queryresult

import com.fasterxml.jackson.databind.JsonNode

interface ColumnValue<RAW_VALUE : Any, JSON_VALUE : JsonNode> {

    val rawValue: RAW_VALUE

    val jsonValue: JSON_VALUE

}