package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.FloatNode
import ru.sui.bi.core.columnvalue.ColumnValue

class FloatColumnValue(override val value: Float) : ColumnValue<Float, FloatNode> {

    override val jsonValue: FloatNode
        get() = FloatNode.valueOf(value)

}