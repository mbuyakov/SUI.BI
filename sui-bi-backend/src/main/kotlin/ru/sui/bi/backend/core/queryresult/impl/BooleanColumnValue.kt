package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.BooleanNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class BooleanColumnValue(override val rawValue: Boolean) : ColumnValue<Boolean, BooleanNode> {

    override val jsonValue: BooleanNode
        get() = BooleanNode.valueOf(rawValue)

}