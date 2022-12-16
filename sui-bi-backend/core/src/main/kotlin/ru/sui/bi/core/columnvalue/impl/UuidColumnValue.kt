package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.core.columnvalue.ColumnValue
import java.util.*

class UuidColumnValue(override val value: UUID) : ColumnValue<UUID, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(value.toString())

}