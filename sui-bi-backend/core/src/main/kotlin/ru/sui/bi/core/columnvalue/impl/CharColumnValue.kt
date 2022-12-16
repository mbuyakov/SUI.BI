package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.core.columnvalue.ColumnValue

class CharColumnValue(override val value: Char) : ColumnValue<Char, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(value.toString())

}