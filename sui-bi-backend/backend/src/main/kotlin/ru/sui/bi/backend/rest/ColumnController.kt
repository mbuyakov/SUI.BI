package ru.sui.bi.backend.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import ru.sui.bi.backend.dto.ColumnDto
import ru.sui.bi.backend.dto.ErrorDto

@Tag(name = "ColumnApi", description = "Операции с колонками")
@RequestMapping("/api/columns")
interface ColumnController {

    /** Получение колонки по идентификатору */
    @Operation(
        operationId = "getColumnById",
        summary = "Получение колонки по идентификатору",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успех",
                content = [
                    Content(
                        schema = Schema(implementation = ColumnDto::class),
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
                responseCode = "404",
                description = "Колонка не найдена",
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
    @GetMapping("/{id}")
    fun getById(
        @Parameter(name = "id", description = "Идентификатор колонки", `in` = ParameterIn.PATH, example = "1", required = true)
        @PathVariable("id") id: Long
    ): ResponseEntity<ColumnDto>

    /** Получение колонок по идентификатору таблицы */
    @Operation(
        operationId = "getColumnsByTableId",
        summary = "Получение колонок по идентификатору таблицы",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успех",
                content = [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ColumnDto::class)),
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
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
    @GetMapping("/tableId={tableId}")
    fun getByTableId(
        @Parameter(name = "tableId", description = "Идентификатор таблицы", `in` = ParameterIn.PATH, example = "1", required = true)
        @PathVariable("tableId") tableId: Long
    ): List<ColumnDto>

}