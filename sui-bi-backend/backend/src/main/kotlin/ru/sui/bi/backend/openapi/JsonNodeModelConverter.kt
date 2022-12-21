package ru.sui.bi.backend.openapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.type.SimpleType
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.converter.ModelConverterContext
import io.swagger.v3.oas.models.media.JsonSchema
import io.swagger.v3.oas.models.media.Schema
import java.lang.reflect.Type

class JsonNodeModelConverter : ModelConverter {

    override fun resolve(type: AnnotatedType?, context: ModelConverterContext?, chain: Iterator<ModelConverter>?): Schema<*>? {
        if (type != null && type.type != null) {
            val schema = resolve(type.type)

            if (schema != null) {
                return schema
            }
        }

        if (chain != null && chain.hasNext()) {
            return chain.next().resolve(type, context, chain)
        }

        return null
    }

    private fun resolve(type: Type): Schema<*>? {
        if (type !is SimpleType || type.rawClass != JsonNode::class.java) {
            return null
        }

        // Как сделать пустую схему - не разобрался
        return JsonSchema().type("")
    }

}