package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.IntNode
import ru.sui.bi.core.columnvalue.ColumnValue

class IntColumnValue(override val value: Int) : ColumnValue<Int, IntNode> {

    override val jsonValue: IntNode
        get() = IntNode.valueOf(value)

}