package com.dkatalis.atm.model

import java.math.BigDecimal

data class Debt(
    val creditor: String,
    var amount: BigDecimal
){
    fun applyPayment(payment: BigDecimal): BigDecimal {
        return if (payment >= amount) {
            val extra = payment.subtract(amount)
            amount = BigDecimal.ZERO
            extra
        } else {
            amount = amount.subtract(payment)
            BigDecimal.ZERO
        }
    }
}
