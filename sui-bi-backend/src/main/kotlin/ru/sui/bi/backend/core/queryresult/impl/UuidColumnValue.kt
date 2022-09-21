package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.backend.core.queryresult.ColumnValue
import java.util.*

class UuidColumnValue(override val rawValue: UUID) : ColumnValue<UUID, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(rawValue.toString())

}