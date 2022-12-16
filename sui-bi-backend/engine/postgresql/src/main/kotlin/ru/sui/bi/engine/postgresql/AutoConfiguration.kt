package ru.sui.bi.engine.postgresql

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AutoConfiguration {

    @Bean
    fun postgresqlEngineSupportFactory(): PostgresqlEngineSupportFactory {
        return PostgresqlEngineSupportFactory()
    }

}