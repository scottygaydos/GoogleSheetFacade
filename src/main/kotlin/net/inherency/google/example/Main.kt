package net.inherency.google.example

import com.google.api.client.json.gson.GsonFactory
import net.inherency.google.base.GoogleSheetClient
import net.inherency.google.pvt.capitalize
import net.inherency.google.example.ComplexReport.Companion.INCOME
import java.time.LocalDate

internal fun main() {
    val googleAuthJson = readEnvVariable("googleAuthJson")
    val googleSheetId = readEnvVariable("googleSheetId")
    val googleAppName = readEnvVariable("googleAppName")
    val client = GoogleSheetClient(
        GsonFactory(),
        googleSheetId,
        googleAuthJson,
        googleAppName
    )

    //generate a simple report
    val randomName = capitalize(getRandomStringForName())
    val simpleReportRepository = SimpleReportRepositoryExample(client)
    val rows = listOf(
        SimpleReport("John", 35, "a@a.com"),
        SimpleReport("Jacob", 44, "b@b.com"),
        SimpleReport("Jingleheimer", 28, "c@c.com"),
        SimpleReport("Schmidt", 51, "d@d.com"),
        SimpleReport(randomName, -1, "e@e.com")
    )
    simpleReportRepository.writeReport(rows)

    //read the simple report
    val readSimpleReport = ReadSimpleReportRepositoryExample(client)
    val readRows = readSimpleReport.findAllSortedCacheable()
    println("Read ${readRows.size} rows from simple report:")
    readRows.forEach { println(it.toString()) }

    //generate a more complex report
    val complexReportRepositoryExample = ComplexReportRepositoryExample(client)
    val complexRows = mutableListOf(
        ComplexReport(LocalDate.of(2022, 1, 1), "paycheck", 40000, INCOME),
        ComplexReport(LocalDate.of(2022, 1, 2), "food", 1022, "Spending"),
        ComplexReport(LocalDate.of(2022, 1, 8), "paycheck", 40000, INCOME),
        ComplexReport(LocalDate.of(2022, 1, 16), "phone bill", 9007, "Utilities"),
        ComplexReport(LocalDate.of(2022, 1, 15), "paycheck", 40000, INCOME),
        ComplexReport(LocalDate.of(2022, 1, 15), "bonus", 3000, INCOME),
    )
    complexReportRepositoryExample.saveAll(1, 2022, complexRows)

    //create a tab
    val tempTab = "Delete This"
    client.createTabs(listOf(tempTab), 2)

    //move the created tab
    client.moveTab(tempTab, 3)

    //delete the created tab
    client.deleteTabs(listOf(tempTab))
}

internal fun readEnvVariable(varName: String) = System.getenv()[varName]
    ?: throw RuntimeException("Could not read $varName from environment")

internal fun getRandomStringForName() : String {
    val charset = "abcdefghiklmnopqrstuvwxyz"
    return (1..10)
        .map { charset.random() }
        .joinToString("")
}