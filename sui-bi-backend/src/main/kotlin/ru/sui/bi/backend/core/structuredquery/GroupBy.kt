package ru.sui.bi.backend.core.structuredquery

data class GroupBy(
    val field: Long,
    val joinAlias: String? = null
)