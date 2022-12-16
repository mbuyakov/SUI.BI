package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.core.columnvalue.ColumnValue
import java.time.OffsetTime
import java.time.ZoneOffset

class OffsetTimeColumnValue(override val value: OffsetTime) : ColumnValue<OffsetTime, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(value.withOffsetSameInstant(ZoneOffset.UTC).toString())

}