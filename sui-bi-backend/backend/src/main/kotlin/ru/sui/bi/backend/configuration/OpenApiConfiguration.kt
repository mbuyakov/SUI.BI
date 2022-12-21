package ru.sui.bi.backend.configuration

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sui.bi.backend.openapi.JsonNodeModelConverter

@Configuration
class OpenApiConfiguration {

    @Bean
    fun api(): OpenAPI {
        val info = Info().title("SUI BI API").version("v1.0.0")
        return OpenAPI().info(info)
    }

    companion object {
        init {
            ModelConverters.getInstance().addConverter(JsonNodeModelConverter())
        }
    }

}