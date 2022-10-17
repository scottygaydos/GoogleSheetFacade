package net.inherency.google.base

import com.google.api.client.json.gson.GsonFactory
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GoogleSheetSimpleReportRepositoryTest {

    private var googleSheetSimpleReportRepository = TestGoogleSheetSimpleReportRepository()

    @Test
    fun `mapObjectsToSheetRowsLocal should maintain property order when mapping an object`() {
        val input = listOf(
            TestDataClass("d", 1, false, "n1"),
            TestDataClass("dd", 2, true, "n2")
        )
        val output = googleSheetSimpleReportRepository.mapObjectsToSheetRows(input)
        assertEquals(listOf(listOf("d", "1", "false", "n1"), listOf("dd", "2", "true", "n2")), output)
    }

    data class TestDataClass (
        val d: String,
        val e: Int,
        val f: Boolean,
        override val name: String
    ): GoogleSheetSimpleReportNameMatchable

    class TestGoogleSheetSimpleReportRepository : GoogleSheetSimpleReportRepository<TestDataClass> {
        override fun getGoogleSheetClient(): GoogleSheetClient {
            return GoogleSheetClient(GsonFactory(), "abc", "{d: e}", "googleAppName")
        }

        override fun tabName(): String {
            return "TestTabName"
        }

    }
}