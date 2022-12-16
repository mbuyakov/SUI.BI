package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.core.columnvalue.ColumnValue
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeColumnValue(override val value: LocalTime) : ColumnValue<LocalTime, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(DateTimeFormatter.ISO_LOCAL_TIME.format(value))

}