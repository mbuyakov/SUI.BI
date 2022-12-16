package ru.sui.bi.backend.structuredquery.domain

data class StructuredQuery(
    val database: Long,
    val query: Query,
    val type: Type
) {

    enum class Type {
        QUERY, NATIVE;
    }

}