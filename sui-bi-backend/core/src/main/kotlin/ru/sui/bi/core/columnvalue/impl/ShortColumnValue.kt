package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.IntNode
import ru.sui.bi.core.columnvalue.ColumnValue

class ShortColumnValue(override val value: Short) : ColumnValue<Short, IntNode> {

    override val jsonValue: IntNode
        get() = IntNode.valueOf(value.toInt())

}