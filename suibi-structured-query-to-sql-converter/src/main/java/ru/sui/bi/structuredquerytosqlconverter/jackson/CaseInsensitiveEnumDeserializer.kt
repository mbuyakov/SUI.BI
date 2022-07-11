package ru.sui.bi.structuredquerytosqlconverter.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StringDeserializer

class CaseInsensitiveEnumDeserializer<T : Enum<T>>(private val targetClass: Class<T>?) : JsonDeserializer<T>(), ContextualDeserializer {

    private val stringDeserializer = StringDeserializer()

    private constructor() : this(null) // Тупо для джексона

    @Suppress("UNCHECKED_CAST")
    override fun createContextual(context: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*> {
        return property
            ?.type
            ?.rawClass
            ?.let { CaseInsensitiveEnumDeserializer(it as Class<T>) } // T - костыль
            ?: CaseInsensitiveEnumDeserializer<T>() // T - костыль
    }

    override fun deserialize(parser: JsonParser, context: DeserializationContext?): T? {
        return stringDeserializer.deserialize(parser, context)
            ?.toUpperCase()
            ?.let { java.lang.Enum.valueOf(targetClass!!, it) }
    }

}