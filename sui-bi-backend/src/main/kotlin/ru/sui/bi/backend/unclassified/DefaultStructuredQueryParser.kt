package ru.sui.bi.backend.unclassified

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import ru.sui.bi.backend.core.exception.SuiBiException
import ru.sui.bi.backend.core.structuredquery.*

// TODO: Пока пойдет
@Suppress("DuplicatedCode")
class DefaultStructuredQueryParser : StructuredQueryParser {

    private val objectMapper = jacksonObjectMapper()

    override fun parse(data: String): StructuredQuery {
        try {
            return parseInternal(objectMapper.readTree(data) as ObjectNode)
        } catch (exception: SuiBiException) {
            throw exception
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    override fun parse(data: ObjectNode): StructuredQuery {
        try {
            return parseInternal(data)
        } catch (exception: SuiBiException) {
            throw exception
        } catch (exception: Exception) {
            throw SuiBiException(exception.message, exception)
        }
    }

    private fun parseInternal(node: ObjectNode): StructuredQuery {
        val database = node.get("database")?.let { objectMapper.treeToValue<Long?>(it) }
        val type = node.get("type")?.asText(null)?.uppercase()?.let { StructuredQuery.Type.valueOf(it) }

        return StructuredQuery(
            database = database ?: throw SuiBiException("Поле \"database\" не заполнено"),
            query = parseQueryInternal(node.path("query") as ObjectNode),
            type = type ?: throw SuiBiException("Поле \"type\" не заполнено")
        )
    }

    private fun parseQueryInternal(node: ObjectNode): Query {
        val sourceTable = node.get("source-table")?.let { objectMapper.treeToValue<Long?>(it) }
        val limit = node.get("limit")?.let { objectMapper.treeToValue<Int?>(it) }

        return Query(
            sourceTable = sourceTable ?: throw SuiBiException("Поле \"source-table\" не заполнено"),
            fields = parseFieldsInternal(node.path("fields")),
            joins = parseJoinsInternal(node.path("joins")),
            aggregation = parseAggregationsInternal(node.path("aggregation")),
            groupBy = parseGroupByInternal(node.path("group-by")),
            filter = parseFilterTreeInternal(node.path("filter")),
            orderBy = parseOrderByInternal(node.path("order-by")),
            limit = limit
        )
    }

    private fun parseFieldsInternal(node: JsonNode): List<Field>? {
        return parseObjectList(node) { element ->
            val field = element.get("field")?.let { objectMapper.treeToValue<Long?>(it) }
            val joinAlias = element.get("join-alias")?.let { objectMapper.treeToValue<String?>(it) }

            return@parseObjectList Field(
                field = field ?: throw SuiBiException("Поле \"field\" у объекта Field не заполнено"),
                joinAlias = joinAlias
            )
        }
    }

    private fun parseJoinsInternal(node: JsonNode): List<Join>? {
        return parseObjectList(node) { element ->
            val sourceTable = element.get("source-table")?.let { objectMapper.treeToValue<Long?>(it) }
            val leftOn = parseJoinOnInternal(element.path("left-on"))
            val rightOn = parseJoinOnInternal(element.path("right-on"))
            val alias = element.get("alias")?.let { objectMapper.treeToValue<String?>(it) }

            val strategyText = element.get("strategy")?.asText(null)
            val strategy = when (strategyText?.lowercase()?.takeIf { it.isNotBlank() }) {
                null -> null
                "inner" -> Join.Strategy.INNER_JOIN
                "left" -> Join.Strategy.LEFT_JOIN
                "right" -> Join.Strategy.RIGHT_JOIN
                "full" -> Join.Strategy.FULL_JOIN
                else -> throw SuiBiException("Неизвестная стратегия соединения \"$strategyText\"")
            }

            return@parseObjectList Join(
                sourceTable = sourceTable ?: throw SuiBiException("Поле \"source-table\" у объекта Join не заполнено"),
                strategy = strategy,
                leftOn = leftOn ?: throw SuiBiException("Поле \"left-on\" у объекта Join не заполнено"),
                rightOn = rightOn ?: throw SuiBiException("Поле \"right-on\" у объекта Join не заполнено"),
                alias = alias ?: throw SuiBiException("Поле \"alias\" у объекта Join не заполнено")
            )
        }
    }

    private fun parseJoinOnInternal(node: JsonNode): Join.On? {
        if (node.isNull || node.isMissingNode) {
            return null
        }

        if (!node.isObject) {
            throw SuiBiException("Значение $node не является объектом")
        }

        val field = node.get("field")?.let { objectMapper.treeToValue<Long?>(it) }
        val alias = node.get("alias")?.let { objectMapper.treeToValue<String?>(it) }

        return Join.On(
            field = field ?: throw SuiBiException("Поле \"field\" у объекта Join.On не заполнено"),
            alias = alias
        )
    }

    private fun parseAggregationsInternal(node: JsonNode): List<Aggregation>? {
        return parseObjectList(node) { element ->
            val aggFunction = element.get("agg-function")?.let { objectMapper.treeToValue<String?>(it) }
            val field = element.get("field")?.let { objectMapper.treeToValue<Long?>(it) }
            val fieldAlias = element.get("field-alias")?.let { objectMapper.treeToValue<String?>(it) }
            val joinAlias = element.get("join-alias")?.let { objectMapper.treeToValue<String?>(it) }

            return@parseObjectList Aggregation(
                aggFunction = aggFunction ?: throw SuiBiException("Поле \"agg-function\" у объекта Aggregation не заполнено"),
                field = field,
                fieldAlias = fieldAlias ?: throw SuiBiException("Поле \"field-alias\" у объекта Aggregation не заполнено"),
                joinAlias = joinAlias
            )
        }
    }

    private fun parseGroupByInternal(node: JsonNode): List<GroupBy>? {
        return parseObjectList(node) { element ->
            val field = element.get("field")?.let { objectMapper.treeToValue<Long?>(it) }
            val joinAlias = element.get("join-alias")?.let { objectMapper.treeToValue<String?>(it) }

            return@parseObjectList GroupBy(
                field = field ?: throw SuiBiException("Поле \"field\" у объекта GroupBy не заполнено"),
                joinAlias = joinAlias
            )
        }
    }

    private fun parseFilterTreeInternal(node: JsonNode): PredicateTree<Filter>? {
        if (node.isNull || node.isMissingNode) {
            return null
        }

        if (!node.isObject) {
            throw SuiBiException("Значение $node не является объектом")
        }

        val not = node.get("not")?.asBoolean(false) ?: false

        val (predicate, nodesNode) = when {
            node.has("and") -> PredicateTree.Predicate.AND to node.path("and")
            node.has("or") -> PredicateTree.Predicate.OR to node.path("or")
            else -> throw SuiBiException("Неизвестный предикат $node")
        }

        val predicateTreeNodes: List<PredicateTree.Node<Filter>> = when {
            nodesNode.isNull || nodesNode.isMissingNode -> emptyList()
            nodesNode.isArray -> nodesNode.map {
                if (it.has("and") || it.has("or")) {
                    PredicateTree.Node.SubTree(parseFilterTreeInternal(it)!!)
                } else {
                    PredicateTree.Node.Value(parseFilterInternal(it as ObjectNode))
                }
            }
            else -> throw SuiBiException("Неподдерживаемое значение предиката $nodesNode")
        }

        return PredicateTree(
            not = not,
            predicate = predicate,
            nodes = predicateTreeNodes
        )
    }

    private fun parseFilterInternal(node: ObjectNode): Filter {
        val field = node.get("field")?.let { objectMapper.treeToValue<Long?>(it) }
        val joinAlias = node.get("join-alias")?.let { objectMapper.treeToValue<String?>(it) }

        val operationText = node.get("operation")?.asText(null)
        val operation = when (operationText?.lowercase()) {
            "in" -> Filter.Operation.IN
            "not in", "not-in" -> Filter.Operation.NOT_IN
            "=" -> Filter.Operation.EQUAL
            "!=" -> Filter.Operation.NOT_EQUAL
            "empty" -> Filter.Operation.EMPTY
            "not empty", "not-empty" -> Filter.Operation.NOT_EMPTY
            ">" -> Filter.Operation.GREATER_THAN
            ">=" -> Filter.Operation.GREATER_THAN_OR_EQUAL
            "<" -> Filter.Operation.LESS_THAN
            "<=" -> Filter.Operation.LESS_THAN_OR_EQUAL
            "contains" -> Filter.Operation.CONTAINS
            else -> throw SuiBiException("Неизвестная операция \"${operationText}\"")
        }

        val valueNode = node.path("value")
        val value = when {
            valueNode.isNull || valueNode.isMissingNode -> null
            valueNode.isArray -> objectMapper.treeToValue<List<String?>>(valueNode)
            else -> objectMapper.treeToValue<String?>(valueNode)?.let { listOf(it) }
        }

        return Filter(
            field = field ?: throw SuiBiException("Поле \"field\" у объекта Filter не заполнено"),
            operation = operation,
            value = value,
            joinAlias = joinAlias
        )
    }

    private fun parseOrderByInternal(node: JsonNode): List<OrderBy>? {
        return parseObjectList(node) { element ->
            val order = element.get("order")?.asText(null)?.uppercase()?.let { OrderBy.Direction.valueOf(it) }
            val field = element.get("field")?.let { objectMapper.treeToValue<Long?>(it) }
            val fieldAlias = element.get("field-alias")?.let { objectMapper.treeToValue<String?>(it) }
            val joinAlias = element.get("join-alias")?.let { objectMapper.treeToValue<String?>(it) }

            return@parseObjectList OrderBy(
                order = order,
                field = field,
                fieldAlias = fieldAlias,
                joinAlias = joinAlias
            )
        }
    }

    private fun <T> parseObjectList(node: JsonNode, objectParser: (ObjectNode) -> T): List<T>? {
        return when {
            node.isNull || node.isMissingNode -> null
            node.isObject -> listOf(objectParser(node as ObjectNode))
            node.isArray -> node.map { objectParser(it as ObjectNode) }
            else -> throw SuiBiException("Значение $node не является объектом или массивом объектов")
        }
    }

}