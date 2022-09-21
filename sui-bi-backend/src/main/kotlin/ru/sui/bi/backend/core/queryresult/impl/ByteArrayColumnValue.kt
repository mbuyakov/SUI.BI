package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.BinaryNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class ByteArrayColumnValue(override val rawValue: ByteArray) : ColumnValue<ByteArray, BinaryNode> {

    override val jsonValue: BinaryNode
        get() = BinaryNode.valueOf(rawValue)

}