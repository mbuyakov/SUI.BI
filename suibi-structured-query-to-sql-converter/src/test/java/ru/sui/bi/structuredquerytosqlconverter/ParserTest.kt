package ru.sui.bi.structuredquerytosqlconverter

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import ru.sui.bi.structuredquerytosqlconverter.model.*

internal class ParserTest {

    @Test
    fun parseTest() {
        // given
        val parser = createParser()
        val structuredQueryString = """
            {
                "database": 2,
                "query": {
                    "source-table": 617,
                    "joins": [
                        {
                            "strategy": "left",
                            "source-table": 619,
                            "left-on": {
                                "field": 9983
                            },
                            "right-on": {
                                "field": 10009,
                                "alias": "sma_smev_response"
                            },
                            "alias": "sma_smev_response"
                        },
                        {
                            "strategy": "inner",
                            "source-table": 656,
                            "left-on": {
                                "field": 9983
                            },
                            "right-on": {
                                "field": 10113,
                                "alias": "fct_requests"
                            },
                            "alias": "fct_requests"
                        }
                    ],
                    "filter": {
                        "or": [
                            {
                                "field": 9980,
                                "operation": ">=",
                                "value": 30
                            },
                            {
                                "field": 9988,
                                "operation": "in",
                                "value": [
                                    "9000052",
                                    "9000053",
                                    "9000054"
                                ]
                            },
                            {
                                "and": [
                                    {
                                        "field": 10006,
                                        "operation": "not-empty",
                                        "join-alias": "sma_smev_response"
                                    },
                                    {
                                        "field": 10008,
                                        "operation": "empty",
                                        "join-alias": "fct_requests"
                                    }
                                ]
                            }
                        ]
                    },
                    "aggregation": [
                        {
                            "agg-function": "count",
                            "field-alias": "count"
                        },
                        {
                            "agg-function": "min",
                            "field": 9986,
                            "field-alias": "min"
                        },
                        {
                            "agg-function": "min",
                            "field": 10104,
                            "join-alias": "sma_smev_response",
                            "field-alias": "min_1"
                        }
                    ],
                    "group-by": [
                        {
                            "field": 9987
                        },
                        {
                            "field": 9977
                        },
                        {
                            "field": 10104,
                            "join-alias": "sma_smev_response"
                        }
                    ],
                    "limit": 1000,
                    "order-by": [
                        {
                            "order": "asc",
                            "field-alias": "count"
                        },
                        {
                            "order": "asc",
                            "field": 9987
                        },
                        {
                            "order": "desc",
                            "field": 10104,
                            "join-alias": "sma_smev_response"
                        },
                        {
                            "order": "desc",
                            "field-alias": "min_1"
                        }
                    ]
                },
                "type": "query"
            }
        """

        // when
        val structuredQuery = parser.parse(structuredQueryString)

        // then
        val target = StructuredQuery(
            database = 2L,
            query = Query(
                sourceTable = 617,
                joins = listOf(
                    Join(
                        strategy = Join.Strategy.LEFT_JOIN,
                        sourceTable = 619,
                        leftOn = Join.On(field = 9983),
                        rightOn = Join.On(field = 10009, alias = "sma_smev_response"),
                        alias = "sma_smev_response"
                    ),
                    Join(
                        strategy = Join.Strategy.INNER_JOIN,
                        sourceTable = 656,
                        leftOn = Join.On(field = 9983),
                        rightOn = Join.On(field = 10113, alias = "fct_requests"),
                        alias = "fct_requests"
                    )
                ),
                filter = PredicateTree(
                    not = false,
                    predicate = PredicateTree.Predicate.OR,
                    nodes = listOf(
                        PredicateTree.Node.Value(
                            Filter(
                                field = 9980,
                                operation = Filter.Operation.GREATER_THAN_OR_EQUAL,
                                value = listOf("30")
                            )
                        ),
                        PredicateTree.Node.Value(
                            Filter(
                                field = 9988,
                                operation = Filter.Operation.IN,
                                value = listOf("9000052", "9000053", "9000054")
                            )
                        ),
                        PredicateTree.Node.SubTree(
                            PredicateTree(
                                not = false,
                                predicate = PredicateTree.Predicate.AND,
                                nodes = listOf(
                                    PredicateTree.Node.Value(
                                        Filter(
                                            field = 10006,
                                            operation = Filter.Operation.NOT_EMPTY,
                                            joinAlias = "sma_smev_response"
                                        )
                                    ),
                                    PredicateTree.Node.Value(
                                        Filter(
                                            field = 10008,
                                            operation = Filter.Operation.EMPTY,
                                            joinAlias = "fct_requests"
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                aggregation = listOf(
                    Aggregation(aggFunction = "count", fieldAlias = "count"),
                    Aggregation(aggFunction = "min", field = 9986, fieldAlias = "min"),
                    Aggregation(aggFunction = "min", field = 10104, joinAlias = "sma_smev_response", fieldAlias = "min_1")
                ),
                groupBy = listOf(
                    GroupBy(field = 9987),
                    GroupBy(field = 9977),
                    GroupBy(field = 10104, joinAlias = "sma_smev_response")
                ),
                limit = 1000,
                orderBy = listOf(
                    OrderBy(order = OrderBy.Direction.ASC, fieldAlias = "count"),
                    OrderBy(order = OrderBy.Direction.ASC, field = 9987),
                    OrderBy(order = OrderBy.Direction.DESC, field = 10104, joinAlias = "sma_smev_response"),
                    OrderBy(order = OrderBy.Direction.DESC, fieldAlias = "min_1")
                )
            ),
            type = StructuredQuery.Type.QUERY
        )

        assertEquals(target, structuredQuery)
    }

    private fun createParser(): Parser {
        return Parser(ObjectMapper())
    }

}