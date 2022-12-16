package ru.sui.bi.backend.structuredquery.domain

data class Aggregation(
    val aggFunction: String,
    val field: Long? = null,
    val fieldAlias: String,
    val joinAlias: String? = null
)