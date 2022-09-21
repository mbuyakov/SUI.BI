package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.LongNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class LongColumnValue(override val rawValue: Long) : ColumnValue<Long, LongNode> {

    override val jsonValue: LongNode
        get() = LongNode.valueOf(rawValue)

}