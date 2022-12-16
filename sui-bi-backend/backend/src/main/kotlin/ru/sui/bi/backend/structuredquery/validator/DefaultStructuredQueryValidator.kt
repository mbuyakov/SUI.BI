package ru.sui.bi.backend.structuredquery.validator

import ru.sui.bi.backend.structuredquery.domain.StructuredQuery
import ru.sui.bi.core.exception.SuiBiException

class DefaultStructuredQueryValidator : StructuredQueryValidator {

    override fun validate(structuredQuery: StructuredQuery) {
        val query = structuredQuery.query

        // Query содержит объект group-by, но не содержит объекта aggregation
        if (!query.groupBy.isNullOrEmpty() && query.aggregation.isNullOrEmpty()) {
            throw SuiBiException("Необходимо указать агрегации, т.к указаны группирующие поля")
        }

        // Валидируем порядок джоинов
        if (query.joins != null) {
            val availableAliases = mutableSetOf<String>()

            query.joins.forEach { join ->
                availableAliases.add(join.alias)

                listOfNotNull(join.leftOn.alias, join.rightOn.alias).forEach {
                    if (!availableAliases.contains(it)) {
                        throw SuiBiException("Таблица с алиасом $it отсутствует или джоинится после использования")
                    }
                }
            }
        }

        // Проверяем, что fields пустой, если есть group-by
        if (!query.groupBy.isNullOrEmpty() && !query.fields.isNullOrEmpty()) {
            throw SuiBiException("При наличии группировки перечень полей не указывается")
        }

        // Проверяем, что fields пустой, если есть aggregation
        if (!query.aggregation.isNullOrEmpty() && !query.fields.isNullOrEmpty()) {
            throw SuiBiException("При наличии агрегации перечень полей не указывается")
        }

        // Проверяем, что в сортировках указан field или fieldAlias
        query.orderBy?.forEach {
            if (it.field == null && it.fieldAlias == null) {
                throw SuiBiException("Сортировка на содержит \"field\" или \"field-alias\"")
            }
        }

        // Проверяем, что все fieldAlias входят в aggregation
        query.orderBy?.filter { it.fieldAlias != null }?.forEach { orderBy ->
            val allowed = (query.aggregation ?: emptyList()).any { it.fieldAlias == orderBy.fieldAlias }

            if (!allowed) {
                throw SuiBiException("Поле \"field-alias\": ${orderBy.fieldAlias} отсутствует в агрегациях")
            }
        }
    }

}