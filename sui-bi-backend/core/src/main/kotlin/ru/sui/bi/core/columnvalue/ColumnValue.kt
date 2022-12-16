package ru.sui.bi.core.columnvalue

import com.fasterxml.jackson.databind.JsonNode

interface ColumnValue<VALUE : Any, JSON_VALUE : JsonNode> {

    val value: VALUE

    val jsonValue: JSON_VALUE

}