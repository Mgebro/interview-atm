package com.dkatalis.atm.service.commands

import com.dkatalis.atm.service.ATMService
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.validation.annotation.Validated
import java.math.BigDecimal

@Validated
@ShellComponent
class ATMCommands(private val atmService: ATMService) {

    @ShellMethod("Login")
    fun login(
        @ShellOption username: String
    ): String {
        return atmService.login(username)
    }

    @ShellMethod("Logout")
    fun logout(): String {
        return atmService.logout()
    }

    @ShellMethod("Deposit money to account")
    fun deposit(
        @ShellOption amount: BigDecimal
    ): String {
        return atmService.deposit(amount)
    }

    @ShellMethod("Withdraw money from account")
    fun withdraw(
        @ShellOption amount: BigDecimal
    ): String {
        return atmService.withdraw(amount)
    }

    @ShellMethod("Transfer money to another user")
    fun transfer(
        @ShellOption target: String,
        @ShellOption amount: BigDecimal
    ): String {
        return atmService.transfer(target, amount)
    }

    @ShellMethod("Show debts")
    fun debts(): String {
        return atmService.debts()
    }
}
