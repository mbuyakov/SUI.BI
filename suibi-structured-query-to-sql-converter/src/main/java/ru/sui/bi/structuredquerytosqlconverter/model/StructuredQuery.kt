package ru.sui.bi.structuredquerytosqlconverter.model

class StructuredQuery(
    val database: Long,
    val query: Query,
    val type: Type
) {

    enum class Type {
        QUERY, NATIVE;
    }

}