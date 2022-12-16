package ru.sui.bi.backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sui.bi.backend.structuredquery.parser.DefaultStructuredQueryParser
import ru.sui.bi.backend.structuredquery.parser.StructuredQueryParser

@Configuration
class StructuredQueryParserConfiguration {

    @Bean
    fun structuredQueryParser(): StructuredQueryParser {
        return DefaultStructuredQueryParser()
    }

}