package ru.sui.bi.structuredquerytosqlconverter.model

data class Field(
    val field: Long,
    val joinAlias: String? = null
)