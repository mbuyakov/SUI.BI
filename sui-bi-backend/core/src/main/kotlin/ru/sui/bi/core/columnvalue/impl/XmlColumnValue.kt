package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.core.columnvalue.ColumnValue

class XmlColumnValue(override val value: String) : ColumnValue<String, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(value)

}