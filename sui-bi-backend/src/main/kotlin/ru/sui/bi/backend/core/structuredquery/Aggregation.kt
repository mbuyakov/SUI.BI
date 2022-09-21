package ru.sui.bi.backend.core.structuredquery

data class Aggregation(
    val aggFunction: String,
    val field: Long? = null,
    val fieldAlias: String,
    val joinAlias: String? = null
)