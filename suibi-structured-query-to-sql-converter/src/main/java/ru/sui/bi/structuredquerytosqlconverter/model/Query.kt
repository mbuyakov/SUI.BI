package ru.sui.bi.structuredquerytosqlconverter.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ru.sui.bi.structuredquerytosqlconverter.jackson.PredicateTreeDeserializer

class Query(
    @JsonProperty("source-table")
    val sourceTable: Long,
    val fields: List<Field>? = null,
    val joins: List<Join>? = null,
    val aggregation: List<Aggregation>? = null,
    @JsonProperty("group-by")
    val groupBy: List<GroupBy>? = null,
    @JsonDeserialize(using = PredicateTreeDeserializer::class)
    val filter: PredicateTree<Filter>? = null,
    @JsonProperty("order-by")
    val orderBy: List<OrderBy>? = null,
    val limit: Int? = null
)