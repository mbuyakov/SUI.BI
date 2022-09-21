package ru.sui.bi.backend.core.metaschema

import ru.sui.bi.backend.core.enumeration.ColumnType

data class ColumnMetaSchema(
    val name: String,
    val type: ColumnType,
    val rawType: String,
    val isNullable: Boolean
)