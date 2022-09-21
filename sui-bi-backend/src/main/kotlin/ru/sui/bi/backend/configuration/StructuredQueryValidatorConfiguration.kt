package ru.sui.bi.backend.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sui.bi.backend.unclassified.DefaultStructuredQueryValidator
import ru.sui.bi.backend.unclassified.StructuredQueryValidator

@Configuration
class StructuredQueryValidatorConfiguration {

    @Bean
    fun structuredQueryValidator(): StructuredQueryValidator {
        return DefaultStructuredQueryValidator()
    }

}