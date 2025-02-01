package com.dkatalis.atm.model

import java.math.BigDecimal
import java.util.*

data class User(
    val name: String,
    var balance: BigDecimal = BigDecimal.ZERO,
    val debts: Queue<Debt> = LinkedList()
) {
    fun deposit(amount: BigDecimal) {
        balance = balance.add(amount)
    }

    fun withdraw(amount: BigDecimal): Boolean {
        return if (balance >= amount) {
            balance = balance.subtract(amount)
            true
        } else false
    }

    fun addDebt(creditor: String, amount: BigDecimal) {
        debts.add(Debt(creditor, amount))
    }
}
