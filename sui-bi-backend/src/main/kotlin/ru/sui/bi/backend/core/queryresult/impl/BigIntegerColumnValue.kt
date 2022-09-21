package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.BigIntegerNode
import ru.sui.bi.backend.core.queryresult.ColumnValue
import java.math.BigInteger

class BigIntegerColumnValue(override val rawValue: BigInteger) : ColumnValue<BigInteger, BigIntegerNode> {

    override val jsonValue: BigIntegerNode
        get() = BigIntegerNode.valueOf(rawValue)

}