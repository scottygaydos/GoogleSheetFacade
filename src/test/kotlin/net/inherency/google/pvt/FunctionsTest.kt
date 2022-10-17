package net.inherency.google.pvt

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FunctionsTest {

    @Test
    fun `capitalize should uppercase the first letter in a string`() {
        val input = "washington"
        val output = capitalize(input)
        assertEquals("Washington", output)
    }

    @Test
    fun `capitalize should uppercase only the first word`() {
        val input = "this is a sentence."
        val output = capitalize(input)
        assertEquals("This is a sentence.", output)
    }

    @Test
    fun `intCentsToCurrency should format an integer as dollars and cents where the integer represents the total in cents`() {
        val input = 12345
        val output = intCentsToCurrency(input)
        assertEquals("$123.45", output)
    }

    @Test
    fun `intCentsToCurrency should format zero with no problem`() {
        val input = 0
        val output = intCentsToCurrency(input)
        assertEquals("$0.00", output)
    }

    @Test
    fun `intCentsToCurrency should format negative integers with no problem`() {
        val input = -25
        val output = intCentsToCurrency(input)
        assertEquals("-$0.25", output)
    }

}