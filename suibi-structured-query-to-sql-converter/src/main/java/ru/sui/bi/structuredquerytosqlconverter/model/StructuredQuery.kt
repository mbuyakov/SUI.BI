package ru.sui.bi.structuredquerytosqlconverter.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ru.sui.bi.structuredquerytosqlconverter.jackson.CaseInsensitiveEnumDeserializer

class StructuredQuery(
    val database: Long,
    val query: Query,
    @JsonDeserialize(using = CaseInsensitiveEnumDeserializer::class)
    val type: Type
) {

    enum class Type {
        QUERY, NATIVE;
    }

}