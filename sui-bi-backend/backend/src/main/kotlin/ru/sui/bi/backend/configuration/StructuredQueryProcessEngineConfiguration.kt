package ru.sui.bi.backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sui.bi.backend.jpa.repository.ColumnRepository
import ru.sui.bi.backend.jpa.repository.TableRepository
import ru.sui.bi.backend.structuredquery.engine.DefaultStructuredQueryProcessEngine
import ru.sui.bi.backend.structuredquery.engine.StructuredQueryProcessEngine

@Configuration
class StructuredQueryProcessEngineConfiguration {

    @Bean
    fun structuredQueryProcessEngine(
        tableRepository: TableRepository,
        columnRepository: ColumnRepository
    ): StructuredQueryProcessEngine {
        return DefaultStructuredQueryProcessEngine(
            tableRepository = tableRepository,
            columnRepository = columnRepository
        )
    }

}