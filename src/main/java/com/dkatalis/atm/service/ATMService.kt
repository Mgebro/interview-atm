package com.dkatalis.atm.service

import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ATMService {

    fun login(name: String): String {
        return ""
    }

    fun logout(): String {
        return ""
    }

    fun deposit(amount: BigDecimal): String {
        return ""
    }

    fun withdraw(amount: BigDecimal): String {
        return ""
    }

    fun transfer(toUser: String, amount: BigDecimal): String {
        return ""
    }

    fun debts(): String {
        return ""
    }
}
