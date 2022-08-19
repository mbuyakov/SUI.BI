package ru.sui.bi.structuredquerytosqlconverter.model

data class StructuredQuery(
    val database: Long,
    val query: Query,
    val type: Type
) {

    enum class Type {
        QUERY, NATIVE;
    }

}