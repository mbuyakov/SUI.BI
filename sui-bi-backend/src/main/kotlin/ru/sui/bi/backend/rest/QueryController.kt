package ru.sui.bi.backend.rest

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sui.bi.backend.dto.QueryResultDto
import ru.sui.bi.backend.provider.DatabaseClientProvider
import ru.sui.bi.backend.unclassified.StructuredQueryParser
import ru.sui.bi.backend.unclassified.StructuredQueryValidator

@RestController
@RequestMapping("/api/queries")
class QueryController(
    private val structuredQueryParser: StructuredQueryParser,
    private val structuredQueryValidator: StructuredQueryValidator,
    private val databaseClientProvider: DatabaseClientProvider
) {

    @PostMapping("/execute")
    fun executeQuery(structuredQueryString: String): QueryResultDto {
        val structuredQuery = structuredQueryParser.parse(structuredQueryString)

        structuredQueryValidator.validate(structuredQuery)

        val queryResult = databaseClientProvider.get(structuredQuery.database).use { it.query(structuredQuery) }

        return QueryResultDto(
            columns = queryResult.columnNames,
            data = queryResult.rows.map { row -> row.map { it?.jsonValue } }.toList()
        )
    }

}