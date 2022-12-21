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
import ru.sui.bi.backend.dto.ErrorDto
import ru.sui.bi.backend.dto.TableDto

@Tag(name = "TableApi", description = "Операции с таблицами")
@RequestMapping("/api/tables")
interface TableController {

    /** Получение таблицы по идентификатору */
    @Operation(
        operationId = "getTableById",
        summary = "Получение таблицы по идентификатору",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успех",
                content = [
                    Content(
                        schema = Schema(implementation = TableDto::class),
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
        @Parameter(name = "id", description = "Идентификатор таблицы", `in` = ParameterIn.PATH, example = "1", required = true)
        @PathVariable("id") id: Long
    ): ResponseEntity<TableDto>

    /** Получение таблиц по идентификатору БД */
    @Operation(
        operationId = "getTablesByDatabaseId",
        summary = "Получение таблиц по идентификатору БД",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Успех",
                content = [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = TableDto::class)),
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
    @GetMapping("/databaseId={databaseId}")
    fun getByDatabaseId(
        @Parameter(name = "databaseId", description = "Идентификатор БД", `in` = ParameterIn.PATH, example = "1", required = true)
        @PathVariable("databaseId") databaseId: Long
    ): List<TableDto>

}