package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.BinaryNode
import ru.sui.bi.core.columnvalue.ColumnValue

class ByteArrayColumnValue(override val value: ByteArray) : ColumnValue<ByteArray, BinaryNode> {

    override val jsonValue: BinaryNode
        get() = BinaryNode.valueOf(value)

}