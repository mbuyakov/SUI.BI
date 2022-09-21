package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.backend.core.queryresult.ColumnValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateColumnValue(override val rawValue: LocalDate) : ColumnValue<LocalDate, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(DateTimeFormatter.ISO_LOCAL_DATE.format(rawValue))

}