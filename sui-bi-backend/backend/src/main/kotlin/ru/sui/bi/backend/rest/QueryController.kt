package ru.sui.bi.backend.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import ru.sui.bi.backend.dto.ErrorDto
import ru.sui.bi.backend.dto.QueryResultDto

@Tag(name = "QueryApi", description = "Операции с запросами")
@RequestMapping("/api/queries")
interface QueryController {

    @Operation(
        operationId = "executeQuery",
        summary = "Выполнение запроса",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успех",
                content = [
                    Content(
                        schema = Schema(implementation = QueryResultDto::class),
                        mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorDto::class),
                        mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
                ]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorDto::class),
                        mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
                ]
            )
        ]
    )
    @PostMapping("/execute")
    fun executeQuery(@RequestBody structuredQueryString: String): QueryResultDto

}