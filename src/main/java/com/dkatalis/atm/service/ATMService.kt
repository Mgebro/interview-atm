package com.dkatalis.atm.service

import com.dkatalis.atm.model.Debt
import com.dkatalis.atm.model.User
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ATMService {

    private val users: MutableMap<String, User> = mutableMapOf()
    private var currentUser: User? = null

    /**
     * Logs in a user by name. If the user does not exist, a new user is created.
     *
     * @param name the name of the user
     * @return a welcome message including balance and debt information
     */
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

    /**
     * Logs out the current user.
     *
     * @return a goodbye message
     */
    fun logout(): String {
        val user = currentUser
        return if (user == null) {
            "No user logged in!"
        } else {
            currentUser = null
            "Goodbye, ${user.name}!"
        }
    }

    /**
     * Deposits a specified amount into the current user's balance, automatically repaying debts if applicable.
     *
     * @param amount the amount to deposit
     * @return a message indicating balance and remaining debts
     */
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

    /**
     * Withdraws a specified amount from the current user's balance.
     *
     * @param amount the amount to withdraw
     * @return a message indicating success or failure
     */
    fun withdraw(amount: BigDecimal): String {
        val user = currentUser ?: return "Please login first!"
        if (amount <= BigDecimal.ZERO) return "Invalid amount!"
        return if (user.withdraw(amount)) {
            "Your balance is \$${user.balance}"
        } else {
            "Insufficient funds!"
        }
    }

    /**
     * Transfers money to another user, repaying debts if applicable.
     *
     * @param targetUserName the recipient's name
     * @param amount         the transfer amount
     * @return a message indicating transfer success or failure
     */
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

    /**
     * Handles a reverse debt transfer from the sender to the recipient.
     *
     * @param sender      the user initiating the transfer
     * @param recipient   the user who owes the sender
     * @param reverseDebt the debt record being reduced
     * @param amount      the amount to be applied toward the debt
     * @return a message indicating the remaining debt or the result of a regular transfer
     */
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


    /**
     * Transfers money from the sender to the recipient, handling debt if necessary.
     *
     * @param sender    the user sending the money
     * @param recipient the user receiving the money
     * @param amount    the amount to be transferred
     * @return a message indicating the transfer success, remaining balance, or incurred debt
     */
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

    /**
     * Retrieves a list of the current user's outstanding debts.
     *
     * @return a formatted string listing debts
     */
    fun debts(): String {
        val user = currentUser ?: return "Please login first!"
        if (user.debts.isEmpty()) return "No outstanding debts."
        return user.debts.mapIndexed { index, debt ->
            "${index + 1}. Owed \$${debt.amount} to ${debt.creditor}"
        }.joinToString("\n")
    }
}