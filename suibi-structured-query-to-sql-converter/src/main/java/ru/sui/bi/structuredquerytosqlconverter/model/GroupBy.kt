package ru.sui.bi.structuredquerytosqlconverter.model

data class GroupBy(
    val field: Long,
    val joinAlias: String? = null
)