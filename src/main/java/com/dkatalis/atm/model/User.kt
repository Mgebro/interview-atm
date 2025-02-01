package com.dkatalis.atm.model

import java.math.BigDecimal
import java.util.LinkedList
import java.util.Queue

data class User(
    val name: String,
    var balance: BigDecimal = BigDecimal.ZERO,
    val debts: Queue<Debt> = LinkedList()
)
