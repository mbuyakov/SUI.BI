package ru.sui.bi.structuredquerytosqlconverter.model

class OrderBy(
    val order: Direction? = null,
    val field: Long? = null,
    val fieldAlias: String? = null,
    val joinAlias: String? = null
) {

    enum class Direction {
        ASC, DESC;
    }

}