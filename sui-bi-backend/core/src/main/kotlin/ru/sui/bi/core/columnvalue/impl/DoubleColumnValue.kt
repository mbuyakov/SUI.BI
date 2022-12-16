package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.DoubleNode
import ru.sui.bi.core.columnvalue.ColumnValue

class DoubleColumnValue(override val value: Double) : ColumnValue<Double, DoubleNode> {

    override val jsonValue: DoubleNode
        get() = DoubleNode.valueOf(value)

}