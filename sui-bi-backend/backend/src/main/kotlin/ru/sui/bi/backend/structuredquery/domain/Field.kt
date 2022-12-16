package ru.sui.bi.backend.structuredquery.domain

data class Field(
    val field: Long,
    val joinAlias: String? = null
)