package ru.sui.bi.backend.core.structuredquery

data class StructuredQuery(
    val database: Long,
    val query: Query,
    val type: Type
) {

    enum class Type {
        QUERY, NATIVE;
    }

}