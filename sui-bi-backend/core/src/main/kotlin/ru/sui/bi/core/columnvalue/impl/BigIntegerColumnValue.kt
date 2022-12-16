package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.BigIntegerNode
import ru.sui.bi.core.columnvalue.ColumnValue
import java.math.BigInteger

class BigIntegerColumnValue(override val value: BigInteger) : ColumnValue<BigInteger, BigIntegerNode> {

    override val jsonValue: BigIntegerNode
        get() = BigIntegerNode.valueOf(value)

}