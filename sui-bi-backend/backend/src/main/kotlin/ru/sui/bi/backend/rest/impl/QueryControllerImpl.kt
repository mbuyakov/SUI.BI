package ru.sui.bi.backend.rest.impl

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import ru.sui.bi.backend.dto.QueryResultDto
import ru.sui.bi.backend.provider.DatabaseClientProvider
import ru.sui.bi.backend.rest.QueryController
import ru.sui.bi.backend.structuredquery.engine.StructuredQueryProcessEngine
import ru.sui.bi.backend.structuredquery.parser.StructuredQueryParser
import ru.sui.bi.backend.structuredquery.validator.StructuredQueryValidator

@RestController
class QueryControllerImpl(
    private val structuredQueryParser: StructuredQueryParser,
    private val structuredQueryProcessEngine: StructuredQueryProcessEngine,
    private val structuredQueryValidator: StructuredQueryValidator,
    private val databaseClientProvider: DatabaseClientProvider
) : QueryController {

    override fun executeQuery(@RequestBody structuredQueryString: String): QueryResultDto {
        val structuredQuery = structuredQueryParser.parse(structuredQueryString)

        structuredQueryValidator.validate(structuredQuery)

        val queryResult = databaseClientProvider.get(structuredQuery.database).use { client ->
            val rawQuery = structuredQueryProcessEngine.process(client, structuredQuery)
            return@use client.query(rawQuery)
        }

        return QueryResultDto(
            columns = queryResult.columnNames,
            data = queryResult.rows.map { row -> row.map { it?.jsonValue } }.toList()
        )
    }

}