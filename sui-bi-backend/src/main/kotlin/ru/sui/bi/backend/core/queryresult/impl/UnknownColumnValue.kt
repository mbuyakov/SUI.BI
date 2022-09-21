package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.TextNode
import ru.sui.bi.backend.core.queryresult.ColumnValue

class UnknownColumnValue(override val rawValue: Any) : ColumnValue<Any, TextNode> {

    override val jsonValue: TextNode
        get() = TextNode.valueOf(rawValue.toString())

}