package net.inherency.google.base

import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Implement this interface to easily generate a report that consists of a single header at the top of a Google
 * sheet tab and then rows that match that header below.
 */
interface GoogleSheetSimpleReportRepository<T: GoogleSheetSimpleReportNameMatchable>: GoogleSheetReportRepository<T> {

    /**
     * Create a report from the provided rows with object property names as the header.
     */
    fun writeReport(rows: List<T>) {
        if (rows.isEmpty()) {
            return
        }
        val example = rows[0]
        val headers = header(example)
        val columnCount = headers.size
        getGoogleSheetClient().deleteRowsForTab(tabName())
        getGoogleSheetClient().clearAllDataInTab(tabName())
        getGoogleSheetClient().saveAll(tabName(), header(example), mapObjectsToSheetRows(rows), columnCount)
        try {
            getGoogleSheetClient().addAlternatingColorsAndBoldTextToTab(tabName())
        } catch (e: Exception) {
            println("saveAll() -> Could not add alternating colors, probably because sheet already has them. Ignoring. tabName=${tabName()}")
        }

    }

    /**
     * Overridden and ignored because this can be determined at runtime.
     */
    override fun getColumnCountToCreate(): Int = -1

    /**
     * Map a list of objects a list of string lists where each string list represents a row in the target google sheet.
     * @param googleSheetNameMatchables All non-header objects, where each object will become a row.
     * @return A list of all rows, where each inner list is a row
     */
    fun mapObjectsToSheetRows(googleSheetNameMatchables: List<T>): List<List<String>> {
        return googleSheetNameMatchables.map { row ->
            row::class.primaryConstructor!!.parameters.map {constructorParam ->
                val name = constructorParam.name!!
                row::class.declaredMemberProperties.first { it.name == name}.getter.call(row).toString()
            }
        }
    }

    /**
     * The first (header) line in the report, each string represents a column header
     */
    fun header(example: T): List<String> {
        return example.generateHeaders()
    }

    /**
     * Returns a Google sheet client for use by the templated writeReport method.
     * @see writeReport
     */
    fun getGoogleSheetClient(): GoogleSheetClient
}