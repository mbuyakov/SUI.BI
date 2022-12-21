package ru.sui.bi.backend.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.sui.bi.backend.dto.ErrorDto

@Tag(name = "MetaApi", description = "Операции с метасхемой")
@RequestMapping("/api/meta")
interface MetaController {

    /** Обновление метасхемы по идентификатору БД */
    @Operation(
        operationId = "updateMetaByDatabaseId",
        summary = "Обновление метасхемы по идентификатору БД",
        responses = [
            ApiResponse(responseCode = "200", description = "Успех"),
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
    @PostMapping("/{databaseId}")
    fun updateMeta(
        @Parameter(name = "databaseId", description = "Идентификатор БД", `in` = ParameterIn.PATH, example = "1", required = true)
        @PathVariable("databaseId") databaseId: Long
    )

}