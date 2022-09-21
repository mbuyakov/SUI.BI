package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.backend.core.queryresult.ColumnValue
import java.time.OffsetTime
import java.time.ZoneOffset

class OffsetTimeColumnValue(override val rawValue: OffsetTime) : ColumnValue<OffsetTime, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(rawValue.withOffsetSameInstant(ZoneOffset.UTC).toString())

}