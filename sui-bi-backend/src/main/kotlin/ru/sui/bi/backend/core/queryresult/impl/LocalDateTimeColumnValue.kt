package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.backend.core.queryresult.ColumnValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeColumnValue(override val rawValue: LocalDateTime) : ColumnValue<LocalDateTime, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(rawValue))

}