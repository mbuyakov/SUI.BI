package ru.sui.bi.structuredquerytosqlconverter.model

class Aggregation(
    val aggFunction: String,
    val field: Long? = null,
    val fieldAlias: String,
    val joinAlias: String? = null
)