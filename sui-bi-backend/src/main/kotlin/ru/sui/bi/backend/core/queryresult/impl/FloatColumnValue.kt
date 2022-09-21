package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.FloatNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class FloatColumnValue(override val rawValue: Float) : ColumnValue<Float, FloatNode> {

    override val jsonValue: FloatNode
        get() = FloatNode.valueOf(rawValue)

}