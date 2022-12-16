package ru.sui.bi.backend.structuredquery.domain

data class Query(
    val sourceTable: Long,
    val fields: List<Field>? = null,
    val joins: List<Join>? = null,
    val aggregation: List<Aggregation>? = null,
    val groupBy: List<GroupBy>? = null,
    val filter: PredicateTree<Filter>? = null,
    val orderBy: List<OrderBy>? = null,
    val limit: Int? = null,
    val offset: Int? = null
)