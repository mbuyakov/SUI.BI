package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.core.columnvalue.ColumnValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeColumnValue(override val value: LocalDateTime) : ColumnValue<LocalDateTime, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value))

}