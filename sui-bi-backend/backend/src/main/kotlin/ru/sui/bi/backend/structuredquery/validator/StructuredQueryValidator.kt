package ru.sui.bi.backend.structuredquery.validator

import ru.sui.bi.backend.structuredquery.domain.StructuredQuery

interface StructuredQueryValidator {

    fun validate(structuredQuery: StructuredQuery)

}