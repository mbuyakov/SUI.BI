package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class XmlColumnValue(override val rawValue: String) : ColumnValue<String, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(rawValue)

}