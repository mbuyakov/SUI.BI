package ru.sui.bi.backend.core.structuredquery

data class Filter(
    val field: Long,
    val operation: Operation,
    val value: List<String?>? = null,
    val joinAlias: String? = null
) {

    enum class Operation {
        IN,
        NOT_IN,
        EQUAL,
        NOT_EQUAL,
        EMPTY,
        NOT_EMPTY,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
        CONTAINS
    }

}