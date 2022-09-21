package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.backend.core.queryresult.ColumnValue
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeColumnValue(override val rawValue: LocalTime) : ColumnValue<LocalTime, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(DateTimeFormatter.ISO_LOCAL_TIME.format(rawValue))

}