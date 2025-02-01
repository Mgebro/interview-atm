package com.dkatalis.atm.service.commands

import com.dkatalis.atm.service.ATMService
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.validation.annotation.Validated


@Validated
@ShellComponent
class ATMCommands(private val atmService: ATMService) {

    @ShellMethod("Login")
    fun login(
        @ShellOption vararg args: String
    ): String {
        return atmService.login(args[0])
    }

    @ShellMethod("Logout")
    fun logout(): String {
        return atmService.logout()
    }

    @ShellMethod("Deposit money to account")
    fun deposit(
        @ShellOption vararg args: String
    ): String {
        return atmService.deposit(args[0].toBigDecimal())
    }

    @ShellMethod("Withdraw money from account")
    fun withdraw(
        @ShellOption vararg args: String
    ): String {
        return atmService.withdraw(args[0].toBigDecimal())
    }

    @ShellMethod("Transfer money to another user")
    fun transfer(
        @ShellOption vararg args: String
    ): String {
        return atmService.transfer(args[0], args[1].toBigDecimal())
    }

    @ShellMethod("Show debts")
    fun debts(): String {
        return atmService.debts()
    }
}
