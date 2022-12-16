package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.BooleanNode
import ru.sui.bi.core.columnvalue.ColumnValue

class BooleanColumnValue(override val value: Boolean) : ColumnValue<Boolean, BooleanNode> {

    override val jsonValue: BooleanNode
        get() = BooleanNode.valueOf(value)

}