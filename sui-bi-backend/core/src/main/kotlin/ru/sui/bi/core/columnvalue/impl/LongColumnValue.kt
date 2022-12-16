package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.LongNode
import ru.sui.bi.core.columnvalue.ColumnValue

class LongColumnValue(override val value: Long) : ColumnValue<Long, LongNode> {

    override val jsonValue: LongNode
        get() = LongNode.valueOf(value)

}