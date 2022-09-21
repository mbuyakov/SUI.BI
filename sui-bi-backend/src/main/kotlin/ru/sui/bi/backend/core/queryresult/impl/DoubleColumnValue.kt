package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.DoubleNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class DoubleColumnValue(override val rawValue: Double) : ColumnValue<Double, DoubleNode> {

    override val jsonValue: DoubleNode
        get() = DoubleNode.valueOf(rawValue)

}