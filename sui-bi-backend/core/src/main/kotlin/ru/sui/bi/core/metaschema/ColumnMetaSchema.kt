package ru.sui.bi.core.metaschema

import ru.sui.bi.core.enumeration.ColumnType

data class ColumnMetaSchema(
    val name: String,
    val type: ColumnType,
    val nativeType: String,
    val isNullable: Boolean
)