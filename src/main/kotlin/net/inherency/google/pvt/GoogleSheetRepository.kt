package net.inherency.google.pvt

import net.inherency.google.base.GoogleSheetNameMatchable

/**
 * Base interface for any repository that might read from or write to a Google sheet tab.
 * This application only interacts with one Google sheet and multiple tabs within that sheet.
 * In most cases, this is not the interface to implement; it is probably best to implement one or more
 * of the interfaces that extend this one with more specialized behavior.
 */
interface GoogleSheetRepository<T: GoogleSheetNameMatchable> {

    /**
     * The tab name in Google Sheets for this type.
     * @return the tab name
     */
    fun tabName(): String
}