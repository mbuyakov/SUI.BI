package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.IntNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class ShortColumnValue(override val rawValue: Short) : ColumnValue<Short, IntNode> {

    override val jsonValue: IntNode
        get() = IntNode.valueOf(rawValue.toInt())

}