package net.inherency.google.base

/**
 * Implement this interface on a pojo / data class that represents some row-like data
 */
interface GoogleSheetNameMatchable {
    /**
     * This property should be unique per instance within a sheet tab - it is the basis for matching.
     */
    val name: String
}