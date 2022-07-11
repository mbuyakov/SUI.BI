package ru.sui.bi.structuredquerytosqlconverter.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import ru.sui.bi.structuredquerytosqlconverter.model.Filter

class FilterOperationDeserializer : JsonDeserializer<Filter.Operation>() {

    private val stringDeserializer = StringDeserializer()

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Filter.Operation? {
        val value = stringDeserializer.deserialize(parser, context)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.toLowerCase()
            ?: return null

        return Filter.Operation.values().firstOrNull { it.values.contains(value) }
            ?: throw JsonMappingException.from(parser, "Unknown operation '$value'")
    }

}