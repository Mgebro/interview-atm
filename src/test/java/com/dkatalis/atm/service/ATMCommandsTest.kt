package com.dkatalis.atm.service

import com.dkatalis.atm.service.commands.ATMCommands
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ATMCommandsTest {
    private lateinit var atmService: ATMService
    private lateinit var atmCommands: ATMCommands

    @BeforeEach
    fun setUp() {
        atmService = ATMService()
        atmCommands = ATMCommands(atmService)
    }

    @Test
    fun `login command should return success`() {
        val output = atmCommands.login("Alice")
        assertTrue(output.contains("Hello, Alice"), "The welcome message should include the trimmed username")
        assertTrue(output.contains("Your balance is \$0"), "New user should have a zero balance")
    }

    @Test
    fun `logout command should return goodbye message`() {
        atmCommands.login("Alice")
        val output = atmCommands.logout()
        assertEquals("Goodbye, Alice!", output)
    }

    @Test
    fun `logout command should return login first message when not logged in`() {
        val output = atmCommands.logout()
        assertEquals("Please login first!", output)
    }

    @Test
    fun `deposit command should increase balance when no debt exists`() {
        atmCommands.login("Alice")
        val output = atmCommands.deposit(BigDecimal("100"))
        assertTrue(output.contains("Your balance is \$100"), "Balance should increase to 100 when no debt exists")
    }

    @Test
    fun `deposit command should return invalid message for non-positive amount`() {
        atmCommands.login("Alice")
        val outputZero = atmCommands.deposit(BigDecimal("0"))
        val outputNegative = atmCommands.deposit(BigDecimal("-5"))
        assertEquals("Invalid amount!", outputZero)
        assertEquals("Invalid amount!", outputNegative)
    }

    @Test
    fun `withdraw command should decrease balance when sufficient funds exist`() {
        atmCommands.login("Alice")
        atmCommands.deposit(BigDecimal("100"))
        val output = atmCommands.withdraw(BigDecimal("40"))
        assertTrue(output.contains("Your balance is \$60"), "After withdrawing 40 from 100, balance should be 60")
    }

    @Test
    fun `withdraw command should return insufficient funds message when balance is low`() {
        atmCommands.login("Alice")
        atmCommands.deposit(BigDecimal("50"))
        val output = atmCommands.withdraw(BigDecimal("60"))
        assertEquals("Insufficient funds!", output)
    }

    @Test
    fun `transfer command should not allow self-transfer`() {
        atmCommands.login("Alice")
        val output = atmCommands.transfer("Alice", BigDecimal("10"))
        assertEquals("Self-transfer not allowed!", output)
    }

    @Test
    fun `transfer command should fail when target user does not exist`() {
        atmCommands.login("Alice")
        atmCommands.deposit(BigDecimal("50"))
        val output = atmCommands.transfer("Bob", BigDecimal("10"))
        assertEquals("Target user does not exist!", output)
    }

    @Test
    fun `transfer command should succeed when sufficient funds exist`() {
        atmCommands.login("Alice")
        atmCommands.deposit(BigDecimal("100"))
        atmService.login("Bob")
        atmCommands.login("Alice")
        val output = atmCommands.transfer("Bob", BigDecimal("50"))
        assertTrue(output.contains("Transferred \$50"), "Expected a successful transfer message")
        assertTrue(output.contains("Your balance is \$50"), "Alice's balance should now be 50")
        val bobOutput = atmCommands.login("Bob")
        assertTrue(bobOutput.contains("Your balance is \$50"), "Bob should have received 50")
    }

    @Test
    fun `transfer command with insufficient funds should create a debt`() {
        atmCommands.login("Alice")
        atmCommands.login("Bob")
        atmCommands.login("Alice")
        val output = atmCommands.transfer("Bob", BigDecimal("20"))
        assertTrue(output.contains("Transferred \$0"), "No funds should be transferred")
        assertTrue(output.contains("Owed \$20 to Bob"), "A full debt of 20 should be recorded")
    }

    @Test
    fun `debts command should return 'No outstanding debts' when there are none`() {
        atmCommands.login("Alice")
        val output = atmCommands.debts()
        assertEquals("No outstanding debts.", output)
    }

    @Test
    fun `debts command should list outstanding debts in FIFO order`() {
        atmCommands.login("Alice")
        atmCommands.login("Bob")
        atmCommands.login("Charlie")
        atmCommands.login("Alice")
        atmCommands.transfer("Bob", BigDecimal("3"))
        atmCommands.transfer("Charlie", BigDecimal("5"))
        val output = atmCommands.debts()
        val expectedLine1 = "1. Owed \$3 to Bob"
        val expectedLine2 = "2. Owed \$5 to Charlie"
        assertTrue(output.contains(expectedLine1), "Debt for Bob should appear as first entry")
        assertTrue(output.contains(expectedLine2), "Debt for Charlie should appear as second entry")
    }
}