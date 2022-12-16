package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.core.columnvalue.ColumnValue
import java.time.OffsetDateTime

class OffsetDateTimeColumnValue(override val value: OffsetDateTime) : ColumnValue<OffsetDateTime, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(value.toInstant().toString())

}