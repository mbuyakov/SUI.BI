package ru.sui.bi.structuredquerytosqlconverter.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import ru.sui.bi.structuredquerytosqlconverter.model.PredicateTree

const val NOT_KEY = "not"
const val AND_KEY = "and"
const val OR_KEY = "or"

class PredicateTreeDeserializer<T>(private val targetClass: Class<T>?) : JsonDeserializer<PredicateTree<T>>(), ContextualDeserializer {

    private constructor() : this(null) // Тупо для джексона

    override fun createContextual(context: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*> {
        return property
            ?.type
            ?.bindings
            ?.typeParameters
            ?.firstOrNull()
            ?.rawClass
            ?.let { PredicateTreeDeserializer(it) }
            ?: PredicateTreeDeserializer<Any>()
    }

    override fun deserialize(parser: JsonParser, context: DeserializationContext?): PredicateTree<T>? {
        val node = parser.readValueAsTree<JsonNode?>()

        if (node == null || node.isNull || node.isMissingNode) {
            return null
        }

        if (!node.isObject) {
            throw JsonMappingException.from(parser, "Unsupported node type '${node.nodeType}'")
        }

        val not = node.path(NOT_KEY).asBoolean(false)

        val (predicate, nodesNode) = when {
            node.has(AND_KEY) -> PredicateTree.Predicate.AND to node.path("and")
            node.has(OR_KEY) -> PredicateTree.Predicate.OR to node.path("or")
            else -> throw JsonMappingException.from(parser, "Unknown predicated node")
        }

        val predicateTreeNodes: List<PredicateTree.Node<T>> = when {
            nodesNode.isNull -> emptyList()
            nodesNode.isMissingNode -> emptyList()
            nodesNode.isArray -> deserializePredicateTreeNodes(parser.codec, node as ArrayNode, context)
            else -> throw JsonMappingException.from(parser, "Unsupported predicate node value type '${nodesNode.nodeType}'")
        }

        return PredicateTree(not, predicate, predicateTreeNodes)
    }

    private fun deserializePredicateTreeNodes(codec: ObjectCodec, node: ArrayNode, context: DeserializationContext?): List<PredicateTree.Node<T>> {
        return node.map {
            if (it.has(AND_KEY) || it.has(OR_KEY)) {
                PredicateTree.Node.SubTree(deserialize(codec.treeAsTokens(it), context)!!)
            } else {
                PredicateTree.Node.Value(codec.treeToValue(it, targetClass))
            }
        }
    }

}