package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.IntNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class IntColumnValue(override val rawValue: Int) : ColumnValue<Int, IntNode> {

    override val jsonValue: IntNode
        get() = IntNode.valueOf(rawValue)

}