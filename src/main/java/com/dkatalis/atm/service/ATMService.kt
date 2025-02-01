package com.dkatalis.atm.service

import com.dkatalis.atm.model.Debt
import com.dkatalis.atm.model.User
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ATMService {
    private val users: MutableMap<String, User> = mutableMapOf()
    private var currentUser: User? = null

    fun login(name: String): String {
        val trimmedName = name.trim()
        val user = users.getOrPut(trimmedName) { User(trimmedName) }
        currentUser = user

        val debtsOwedByUser = if (user.debts.isNotEmpty()) {
            user.debts.map { "Owed \$${it.amount} to ${it.creditor}" }
        } else emptyList()

        val debtsOwedToUser = users.filter { it.key != user.name }
            .mapNotNull { (_, otherUser) ->
                val totalOwed = otherUser.debts
                    .filter { it.creditor == user.name }
                    .fold(BigDecimal.ZERO) { acc, debt -> acc.add(debt.amount) }
                if (totalOwed > BigDecimal.ZERO) "Owed \$${totalOwed} from ${otherUser.name}" else null
            }

        val debtInfo = (debtsOwedByUser + debtsOwedToUser).joinToString("\n")
        val infoOutput = if (debtInfo.isNotBlank()) "\n$debtInfo" else ""
        return "Hello, ${user.name}!\nYour balance is \$${user.balance}$infoOutput"
    }

    fun logout(): String {
        val user = currentUser
        return if (user == null) {
            "No user logged in!"
        } else {
            currentUser = null
            "Goodbye, ${user.name}!"
        }
    }

    fun deposit(amount: BigDecimal): String {
        val user = currentUser ?: return "Please login first!"
        if (amount <= BigDecimal.ZERO) return "Invalid amount!"

        val messages = mutableListOf<String>()

        val remainingAmount = applyDepositToDebts(user, amount, messages)

        user.deposit(remainingAmount)
        messages.add("Your balance is \$${user.balance}")
        if (user.debts.isNotEmpty()) {
            user.debts.forEach { debt ->
                messages.add("Owed \$${debt.amount} to ${debt.creditor}")
            }
        }
        return messages.joinToString("\n")
    }

    /**
     * Applies the [depositAmount] to pay off the user’s debts.
     *
     * @param user The user making the deposit.
     * @param depositAmount The total deposit amount.
     * @param messages A list to record messages regarding debt payments.
     * @return The remaining deposit amount (if any) after debts are paid.
     */
    private fun applyDepositToDebts(user: User, depositAmount: BigDecimal, messages: MutableList<String>): BigDecimal {
        var remaining = depositAmount
        while (remaining > BigDecimal.ZERO && user.debts.isNotEmpty()) {
            val currentDebt = user.debts.peek()
            val payment = if (remaining < currentDebt.amount) remaining else currentDebt.amount

            val creditor = users[currentDebt.creditor]
                ?: throw IllegalStateException("Creditor ${currentDebt.creditor} not found.")
            creditor.deposit(payment)
            messages.add("Transferred \$${payment} to ${currentDebt.creditor}")

            currentDebt.applyPayment(payment)

            if (currentDebt.amount == BigDecimal.ZERO) {
                user.debts.poll()
            }
            remaining = remaining.subtract(payment)
        }
        return remaining
    }

    fun withdraw(amount: BigDecimal): String {
        val user = currentUser ?: return "Please login first!"
        if (amount <= BigDecimal.ZERO) return "Invalid amount!"
        return if (user.withdraw(amount)) {
            "Your balance is \$${user.balance}"
        } else {
            "Insufficient funds!"
        }
    }

    fun transfer(targetUserName: String, amount: BigDecimal): String {
        val sender = currentUser ?: return "Please login first!"
        if (amount <= BigDecimal.ZERO) return "Invalid transfer amount!"

        val trimmedTarget = targetUserName.trim()
        if (sender.name == trimmedTarget) return "Self-transfer not allowed!"

        val recipient = users[trimmedTarget] ?: return "Target user does not exist!"

        val reverseDebt = recipient.debts.find { it.creditor == sender.name }
        return if (reverseDebt != null) {
            handleReverseDebtTransfer(sender, recipient, reverseDebt, amount)
        } else {
            handleRegularTransfer(sender, recipient, amount)
        }
    }

    private fun handleReverseDebtTransfer(sender: User, recipient: User, reverseDebt: Debt, amount: BigDecimal): String {
        return if (amount <= reverseDebt.amount) {
            reverseDebt.amount = reverseDebt.amount.subtract(amount)
            if (reverseDebt.amount == BigDecimal.ZERO) {
                recipient.debts.remove(reverseDebt)
            }
            "Your balance is \$${sender.balance}\nOwed \$${reverseDebt.amount} from ${recipient.name}"
        } else {
            val extraAmount = amount.subtract(reverseDebt.amount)
            recipient.debts.remove(reverseDebt)
            handleRegularTransfer(sender, recipient, extraAmount)
        }
    }

    private fun handleRegularTransfer(sender: User, recipient: User, amount: BigDecimal): String {
        return if (sender.balance >= amount) {
            sender.withdraw(amount)
            recipient.deposit(amount)
            "Transferred \$${amount} to ${recipient.name}\nYour balance is \$${sender.balance}"
        } else {
            val available = sender.balance
            sender.withdraw(available)
            recipient.deposit(available)
            val remainingDebt = amount.subtract(available)
            sender.addDebt(recipient.name, remainingDebt)
            "Transferred \$${available} to ${recipient.name}\nYour balance is \$0\nOwed \$${remainingDebt} to ${recipient.name}"
        }
    }

    fun debts(): String {
        val user = currentUser ?: return "Please login first!"
        if (user.debts.isEmpty()) return "No outstanding debts."
        return user.debts.mapIndexed { index, debt ->
            "${index + 1}. Owed \$${debt.amount} to ${debt.creditor}"
        }.joinToString("\n")
    }
}