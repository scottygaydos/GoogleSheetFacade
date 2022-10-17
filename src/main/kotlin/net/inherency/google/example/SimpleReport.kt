package net.inherency.google.example

import net.inherency.google.base.GoogleSheetSimpleReportNameMatchable

data class SimpleReport(
    override val name: String,
    val age: Int,
    val email: String
): GoogleSheetSimpleReportNameMatchable
