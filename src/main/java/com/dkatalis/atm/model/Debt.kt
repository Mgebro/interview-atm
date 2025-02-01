package com.dkatalis.atm.model

import java.math.BigDecimal

data class Debt(
    val creditor: String,
    var amount: BigDecimal
)
