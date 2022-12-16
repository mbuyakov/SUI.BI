package ru.sui.bi.backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sui.bi.backend.structuredquery.validator.DefaultStructuredQueryValidator
import ru.sui.bi.backend.structuredquery.validator.StructuredQueryValidator

@Configuration
class StructuredQueryValidatorConfiguration {

    @Bean
    fun structuredQueryValidator(): StructuredQueryValidator {
        return DefaultStructuredQueryValidator()
    }

}