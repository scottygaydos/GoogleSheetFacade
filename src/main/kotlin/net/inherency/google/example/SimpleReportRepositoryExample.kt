package net.inherency.google.example

import net.inherency.google.base.GoogleSheetClient
import net.inherency.google.base.GoogleSheetSimpleReportRepository

internal class SimpleReportRepositoryExample(
    private val googleSheetClient: GoogleSheetClient
): GoogleSheetSimpleReportRepository<SimpleReport> {

    override fun getGoogleSheetClient(): GoogleSheetClient {
        return googleSheetClient
    }

    override fun tabName(): String {
        return "SimpleReport"
    }
}