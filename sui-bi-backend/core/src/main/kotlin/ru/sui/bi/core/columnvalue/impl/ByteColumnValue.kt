package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.IntNode
import ru.sui.bi.core.columnvalue.ColumnValue

class ByteColumnValue(override val value: Byte) : ColumnValue<Byte, IntNode> {

    override val jsonValue: IntNode
        get() = IntNode.valueOf(value.toInt())

}