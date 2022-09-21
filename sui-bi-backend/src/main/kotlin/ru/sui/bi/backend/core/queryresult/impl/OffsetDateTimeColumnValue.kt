package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.backend.core.queryresult.ColumnValue
import java.time.OffsetDateTime

class OffsetDateTimeColumnValue(override val rawValue: OffsetDateTime) : ColumnValue<OffsetDateTime, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(rawValue.toInstant().toString())

}