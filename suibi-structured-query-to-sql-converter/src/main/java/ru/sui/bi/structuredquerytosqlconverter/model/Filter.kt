package ru.sui.bi.structuredquerytosqlconverter.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ru.sui.bi.structuredquerytosqlconverter.jackson.FilterOperationDeserializer
import ru.sui.bi.structuredquerytosqlconverter.jackson.FilterValueDeserializer

class Filter(
    val field: Long,
    @JsonDeserialize(using = FilterOperationDeserializer::class)
    val operation: Operation,
    @JsonDeserialize(using = FilterValueDeserializer::class)
    val value: List<String?>? = null,
    @JsonProperty("join-alias")
    val joinAlias: String? = null
) {

    enum class Operation(val values: Set<String>) {
        IN(setOf("in")),
        NOT_IN(setOf("not in", "not-in")),
        EQUAL(setOf("=")),
        NOT_EQUAL(setOf("!=")),
        EMPTY(setOf("empty")),
        NOT_EMPTY(setOf("not empty", "not-empty")),
        GREATER_THAN(setOf(">")),
        GREATER_THAN_OR_EQUAL(setOf(">=")),
        LESS_THAN(setOf("<")),
        LESS_THAN_OR_EQUAL(setOf("<=")),
        CONTAINS(setOf("contains"))
    }

}