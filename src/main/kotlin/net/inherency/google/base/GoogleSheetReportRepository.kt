package net.inherency.google.base

import net.inherency.google.pvt.GoogleSheetRepository

/**
 * This interface represents the basic tools needed to generate a report to a Google sheet tab.
 * If the output report is a simple header and rows under the header, then it is probably better
 * to implement the simple report interface. When using this interface to create a more complex report,
 * the report creator is separately required to write functions to:
 * 1. Define header(s) in appropriate places
 * 2. Map objects to report rows
 * 3. Deleting existing rows if desired
 * 4. Save report rows (including headers) to the Google sheet tab with some new function.
 * @see net.inherency.google.base.GoogleSheetSimpleReportRepository
 */
interface GoogleSheetReportRepository<T: GoogleSheetNameMatchable>: GoogleSheetRepository<T> {

    /**
     * When re-creating this tab in the sheet, the tab will have this many columns
     */
    fun getColumnCountToCreate(): Int
}