package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.core.columnvalue.ColumnValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateColumnValue(override val value: LocalDate) : ColumnValue<LocalDate, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(DateTimeFormatter.ISO_LOCAL_DATE.format(value))

}