package ru.sui.bi.structuredquerytosqlconverter.model

import com.fasterxml.jackson.annotation.JsonProperty

class Aggregation(
    @JsonProperty("agg-function")
    val aggFunction: String,
    val field: Long? = null,
    @JsonProperty("field-alias")
    val fieldAlias: String,
    @JsonProperty("join-alias")
    val joinAlias: String? = null
)