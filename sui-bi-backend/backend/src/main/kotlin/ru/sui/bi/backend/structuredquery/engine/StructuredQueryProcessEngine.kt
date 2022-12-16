package ru.sui.bi.backend.structuredquery.engine

import ru.sui.bi.backend.structuredquery.domain.StructuredQuery
import ru.sui.bi.core.DatabaseClient
import ru.sui.bi.core.Query

interface StructuredQueryProcessEngine {

    fun process(client: DatabaseClient<out Query>, structuredQuery: StructuredQuery): Query

}