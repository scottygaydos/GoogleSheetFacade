package net.inherency.google.example

import net.inherency.google.base.GoogleSheetClient
import net.inherency.google.base.GoogleSheetReportRepository
import net.inherency.google.example.ComplexReport.Companion.INCOME
import net.inherency.google.pvt.intCentsToCurrency
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

internal class ComplexReportRepositoryExample(
    private val googleSheetClient: GoogleSheetClient
): GoogleSheetReportRepository<ComplexReport> {

    override fun getColumnCountToCreate() = 8

    override fun tabName() = "ComplexReport"

    fun saveAll(reportMonth: Int, reportYear: Int, currentMonthBudgetRows: List<ComplexReport>) {
        googleSheetClient.deleteRowsForTab(tabName())
        val currentMonthHeader = "${Month.of(reportMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH)}, $reportYear"
        googleSheetClient.clearAllDataInTab(tabName())

        val (currentMonthRows, budgetRemaining) = mapRows(currentMonthBudgetRows)

        val debitHeader = listOf("Date", "Description", "Category", "Amount")
        val creditHeader = listOf("Deposit Date", "Description", "Amount")

        val reportRows = mutableListOf(
            listOf(currentMonthHeader, "", "Remaining Budget: ${intCentsToCurrency(budgetRemaining)}"),
            listOf(""),
            debitHeader.plus("").plus(creditHeader)
        )
        reportRows.addAll(currentMonthRows)
        googleSheetClient.saveAllRowsSelfFormatted(tabName(), reportRows)
    }

    private fun mapRows(currentMonthBudgetRows: List<ComplexReport>): Pair<MutableList<List<String>>, Int> {
        val (credits, debits) = currentMonthBudgetRows.partition { it.category == INCOME }
        val creditsTotal = sumTransactions(credits)
        val debitsTotal = sumTransactions(debits)
        val budgetRemaining = creditsTotal - debitsTotal
        val debitsStrings: List<List<String>> = debits.map { listOf(
            it.transactionDate.toString(),
            it.transactionDescription,
            it.category,
            intCentsToCurrency(it.amount)
        ) }.plus(listOf(listOf("", "", "Total", intCentsToCurrency(debitsTotal))))
        val creditsStrings: List<List<String>> = credits.map { listOf(
            it.transactionDate.toString(),
            it.transactionDescription,
            intCentsToCurrency(it.amount)
        ) }.plus(listOf(listOf("", "Total", intCentsToCurrency(creditsTotal))))
        val currentMonthRows = mutableListOf<List<String>>()
        IntRange(0, currentMonthBudgetRows.size - 1).forEach {
            val debitRow = debitsStrings.getOrNull(it)
            val creditRow = creditsStrings.getOrNull(it)
            if (debitRow != null || creditRow != null) {
                currentMonthRows.add(mapRow(debitRow, creditRow))
            }
        }
        return Pair(currentMonthRows, budgetRemaining)
    }

    private fun sumTransactions(rows: List<ComplexReport>) = rows.sumOf { it.amount }

    private fun mapRow(debitRow: List<String>?, creditRow: List<String>?): List<String> {
        val values = mutableListOf<String>()
        values.addAll(debitRow ?: listOf("", "", "", ""))
        values.add("")
        values.addAll(creditRow ?: listOf("", "", ""))
        return values
    }
}