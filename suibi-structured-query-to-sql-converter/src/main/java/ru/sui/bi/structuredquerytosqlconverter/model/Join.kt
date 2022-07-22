package ru.sui.bi.structuredquerytosqlconverter.model

class Join(
    val sourceTable: Long,
    val strategy: Strategy? = null,
    val leftOn: On,
    val rightOn: On,
    val alias: String
) {

    enum class Strategy {
        INNER_JOIN,
        LEFT_JOIN,
        RIGHT_JOIN,
        FULL_JOIN
    }

    class On(
        val field: Long,
        val alias: String? = null
    )

}