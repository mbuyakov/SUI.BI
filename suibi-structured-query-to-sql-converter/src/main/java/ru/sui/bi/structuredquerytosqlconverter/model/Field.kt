package ru.sui.bi.structuredquerytosqlconverter.model

import com.fasterxml.jackson.annotation.JsonProperty

class Field(
    val field: Long,
    @JsonProperty("join-alias")
    val joinAlias: String? = null
)