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
        @ShellOption vararg args: String
    ): String {
        if (args.size != 1) {
            return "Error: Login requires exactly 1 argument (username)."
        }
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
        if (args.size != 1) {
            return "Error: Deposit requires exactly 1 argument (amount)."
        }
        return try {
            val amount = args[0].toBigDecimal()
            if (amount <= BigDecimal.ZERO) {
                "Error: Deposit amount must be greater than zero."
            } else {
                atmService.deposit(amount)
            }
        } catch (e: NumberFormatException) {
            "Error: Invalid amount format. Please enter a valid number."
        }
    }

    @ShellMethod("Withdraw money from account")
    fun withdraw(
        @ShellOption vararg args: String
    ): String {
        if (args.size != 1) {
            return "Error: Withdraw requires exactly 1 argument (amount)."
        }
        return try {
            val amount = args[0].toBigDecimal()
            if (amount <= BigDecimal.ZERO) {
                "Error: Withdraw amount must be greater than zero."
            } else {
                atmService.withdraw(amount)
            }
        } catch (e: NumberFormatException) {
            "Error: Invalid amount format. Please enter a valid number."
        }
    }

    @ShellMethod("Transfer money to another user")
    fun transfer(
        @ShellOption vararg args: String
    ): String {
        if (args.size != 2) {
            return "Error: Transfer requires exactly 2 arguments (recipient username and amount)."
        }
        return try {
            val amount = args[1].toBigDecimal()
            if (amount <= BigDecimal.ZERO) {
                "Error: Transfer amount must be greater than zero."
            } else {
                atmService.transfer(args[0], amount)
            }
        } catch (e: NumberFormatException) {
            "Error: Invalid amount format. Please enter a valid number."
        }
    }

    @ShellMethod("Show debts")
    fun debts(): String {
        return atmService.debts()
    }
}