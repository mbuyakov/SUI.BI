package ru.sui.bi.structuredquerytosqlconverter.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

class FilterValueDeserializer : JsonDeserializer<List<String?>>() {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): List<String?>? {
        val node = parser.readValueAsTree<JsonNode?>()

        return when {
            node == null -> null
            node.isNull -> null
            node.isMissingNode -> null
            node.isArray -> parser.codec.treeAsTokens(node).readValueAs(object: TypeReference<List<String?>>() {})
            else -> parser.codec.treeToValue(node, String::class.java)?.let { listOf(it) }
        }
    }

}