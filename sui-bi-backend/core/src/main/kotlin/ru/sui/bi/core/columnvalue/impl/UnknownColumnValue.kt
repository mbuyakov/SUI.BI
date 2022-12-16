package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.core.columnvalue.ColumnValue

class UnknownColumnValue(override val value: Any) : ColumnValue<Any, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(value.toString())

}