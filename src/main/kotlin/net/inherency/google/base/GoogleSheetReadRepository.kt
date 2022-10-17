package net.inherency.google.base

import net.inherency.google.pvt.GoogleSheetRepository

/**
 * An interface for sheets that are meant as input / reading; if the application writes to this repository,
 * then the application class should implement a different interface for that purpose.
 */
interface GoogleSheetReadRepository<T: GoogleSheetNameMatchable>: GoogleSheetRepository<T> {
    /**
     * This method should find all non-header rows, sort results AND implement caching to ensure the app does not hit
     * sheet read limits. If mapping is necessary in a non-cached situation, mapping can likely delegate to mapRowsFromSheetToObjects.
     * @see mapRowsFromSheetToObjects
     *
     * @return list of sorted, cached type instances
     */
    fun findAllSortedCacheable(): List<T>

    /**
     * Defines how to map one row from the Google sheet to the desired object.
     * @param row One row from a Google sheet tab. Each item in the list is one column.
     * @return The resulting mapped object.
     */
    fun mapRowsFromSheetToObjects(row: List<String>): T
}