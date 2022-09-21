package ru.sui.bi.backend.unclassified

import com.fasterxml.jackson.databind.node.ObjectNode
import ru.sui.bi.backend.core.structuredquery.StructuredQuery

interface StructuredQueryParser {

    fun parse(data: String): StructuredQuery

    fun parse(data: ObjectNode): StructuredQuery

}