package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.IntNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class ByteColumnValue(override val rawValue: Byte) : ColumnValue<Byte, IntNode> {

    override val jsonValue: IntNode
        get() = IntNode.valueOf(rawValue.toInt())

}