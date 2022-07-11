package ru.sui.bi.structuredquerytosqlconverter.model

import com.fasterxml.jackson.annotation.JsonProperty

class Join(
    @JsonProperty("source-table")
    val sourceTable: Long,
    val strategy: Strategy? = null,
    @JsonProperty("left-on")
    val leftOn: On,
    @JsonProperty("right-on")
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