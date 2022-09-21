package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class CharColumnValue(override val rawValue: Char) : ColumnValue<Char, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(rawValue.toString())

}