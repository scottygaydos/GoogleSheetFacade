package net.inherency.google.base

import net.inherency.google.pvt.capitalize

/**
 * Implement this interface on some row-like pojo/data class for use with a GoogleSheetSimpleReportRepository.
 */
interface GoogleSheetSimpleReportNameMatchable: GoogleSheetNameMatchable {
    fun generateHeaders(): List<String> {
        return this::class.java.declaredFields.map { capitalize(it.name) }
    }
}