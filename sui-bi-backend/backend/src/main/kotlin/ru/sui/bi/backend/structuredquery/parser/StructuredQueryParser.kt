package ru.sui.bi.backend.structuredquery.parser

import com.fasterxml.jackson.databind.node.ObjectNode
import ru.sui.bi.backend.structuredquery.domain.StructuredQuery

interface StructuredQueryParser {

    fun parse(data: String): StructuredQuery

    fun parse(data: ObjectNode): StructuredQuery

}