package ru.sui.bi.core.columnvalue.impl

import com.fasterxml.jackson.databind.node.DecimalNode
import ru.sui.bi.core.columnvalue.ColumnValue
import java.math.BigDecimal

class BigDecimalColumnValue(override val value: BigDecimal) : ColumnValue<BigDecimal, DecimalNode> {

    override val jsonValue: DecimalNode
        get() = DecimalNode.valueOf(value)

}