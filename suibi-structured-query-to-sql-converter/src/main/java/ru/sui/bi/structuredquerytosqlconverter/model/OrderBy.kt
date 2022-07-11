package ru.sui.bi.structuredquerytosqlconverter.model

import com.fasterxml.jackson.annotation.JsonProperty

class OrderBy(
    val order: Direction? = null,
    val field: Long? = null,
    @JsonProperty("field-alias")
    val fieldAlias: String? = null,
    @JsonProperty("join-alias")
    val joinAlias: String? = null
) {

    enum class Direction {
        ASC, DESC;
    }

}