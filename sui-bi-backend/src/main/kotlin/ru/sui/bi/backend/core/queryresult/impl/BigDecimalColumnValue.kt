package ru.sui.bi.backend.core.queryresult.impl

import com.fasterxml.jackson.databind.node.DecimalNode
import ru.sui.bi.backend.core.queryresult.ColumnValue
import java.math.BigDecimal

class BigDecimalColumnValue(override val rawValue: BigDecimal) : ColumnValue<BigDecimal, DecimalNode> {

    override val jsonValue: DecimalNode
        get() = DecimalNode.valueOf(rawValue)

}