package ru.sui.bi.backend.unclassified

import ru.sui.bi.backend.core.structuredquery.StructuredQuery

interface StructuredQueryValidator {

    fun validate(structuredQuery: StructuredQuery)

}