package ru.sui.bi.structuredquerytosqlconverter.model

data class Aggregation(
    val aggFunction: String,
    val field: Long? = null,
    val fieldAlias: String,
    val joinAlias: String? = null
)