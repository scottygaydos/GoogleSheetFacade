package net.inherency.google.example

import net.inherency.google.base.GoogleSheetClient
import net.inherency.google.base.GoogleSheetReadRepository

internal class ReadSimpleReportRepositoryExample(
    private val googleSheetClient: GoogleSheetClient
): GoogleSheetReadRepository<SimpleReport> {

    //use very simple, local caching. This is just an example class.
    private var rows: List<SimpleReport> = emptyList()

    override fun findAllSortedCacheable(): List<SimpleReport> {
        return rows.ifEmpty {
            googleSheetClient.reportNonHeaderRowsFromTab(
                tabName(),
                this::mapRowsFromSheetToObjects
            )
        }
    }

    override fun mapRowsFromSheetToObjects(row: List<String>): SimpleReport {
        return SimpleReport(
            name = row[0],
            age = row[1].toInt(),
            email = row[2]
        )
    }

    override fun tabName(): String {
        return "SimpleReport"
    }
}