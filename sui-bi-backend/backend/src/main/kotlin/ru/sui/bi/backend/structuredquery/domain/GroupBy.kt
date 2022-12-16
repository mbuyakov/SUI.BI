package ru.sui.bi.backend.structuredquery.domain

data class GroupBy(
    val field: Long,
    val joinAlias: String? = null
)