package net.inherency.google.example

import net.inherency.google.base.GoogleSheetNameMatchable
import java.time.LocalDate

data class ComplexReport(
    val transactionDate: LocalDate,
    val transactionDescription: String,
    val amount: Int,
    val category: String,
): GoogleSheetNameMatchable {
    companion object {
        const val INCOME = "Income"
    }
    override val name: String
        get() = transactionDate.toString()+transactionDescription+amount.toString()+category
}