package net.inherency.google.base

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.AddBandingRequest
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.AutoResizeDimensionsRequest
import com.google.api.services.sheets.v4.model.BandedRange
import com.google.api.services.sheets.v4.model.BandingProperties
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.CellData
import com.google.api.services.sheets.v4.model.CellFormat
import com.google.api.services.sheets.v4.model.ClearValuesRequest
import com.google.api.services.sheets.v4.model.Color
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest
import com.google.api.services.sheets.v4.model.DeleteSheetRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.GridProperties
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.RepeatCellRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.TextFormat
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import org.slf4j.LoggerFactory

class GoogleSheetClient(
    private val jsonFactory: GsonFactory,
    private val googleSheetId: String,
    private val googleAuthJson: String,
    private val googleAppName: String) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun deleteRowsForTab(tabName: String, startIndex: Int = 1) {
        val dimensionRange = DimensionRange()
        dimensionRange.sheetId = getIdAndSheetPropertiesForTab(tabName).first
        dimensionRange.dimension = "ROWS"
        dimensionRange.startIndex = startIndex
        val deleteDimensionsRequest = DeleteDimensionRequest()
        deleteDimensionsRequest.range = dimensionRange
        val request = Request()
        request.deleteDimension = deleteDimensionsRequest
        val body = BatchUpdateSpreadsheetRequest()
        body.requests = listOf(request)
        try {
            runBatchUpdate(body)
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode != 400) {
                throw e
            } else {
                log.info("Could not delete rows for $tabName, probably because there aren't any rows to delete")
                log.info("Could not delete rows reason: "+e.localizedMessage)
            }
        }
    }

    fun deleteTabs(tabs: List<String>) {
        val deletes = tabs.map {
            val delete = DeleteSheetRequest()
            delete.sheetId = getSheetIdForTab(it)
            val request = Request()
            request.deleteSheet = delete
            request
        }
        val body = BatchUpdateSpreadsheetRequest()
        body.requests = deletes
        runBatchUpdate(body)
    }

    /**
     * Move a tab from one position to another in the sheet.
     * @param tab Is the tab name.
     * @param destinationIndex 1-based index for where to move the tab.
     * @throws Exception when index is too low (less than 1) or too high (above the count of existing sheet tabs).
     */
    fun moveTab(tab: String, destinationIndex: Int) {
        val sheetIdAndProperties = getIdAndSheetPropertiesForTab(tab)
        val sheetProperties = sheetIdAndProperties.second
        sheetProperties.index = destinationIndex
        val update = UpdateSheetPropertiesRequest()
        update.properties = sheetProperties
        update.fields = "*"
        val request = Request()
        request.updateSheetProperties = update
        val body = BatchUpdateSpreadsheetRequest()
        body.requests = listOf(request)
        runBatchUpdate(body)
    }

    fun createTabs(tabs: List<String>, columnCount: Int) {
        //counter starts at index 1 because we always want the INFORMATION tab to stay first tab.
        var counter = 1
        val requests = tabs.map {
            val gridProperties = GridProperties()
            //TODO: How can I get rid of the blank row?
            //Cannot create with 0 rowCount because then sheets defaults to some big number. :(
            //Creating with rowCount = 1 means we'll have one annoying blank row at the bottom.
            gridProperties.rowCount = 1
            gridProperties.columnCount = columnCount

            val sheetProperties = SheetProperties()
            sheetProperties.gridProperties = gridProperties
            sheetProperties.title = it
            sheetProperties.index = counter
            counter += 1

            val addSheetRequest = AddSheetRequest()
            addSheetRequest.properties = sheetProperties
            val request = Request()
            request.addSheet = addSheetRequest
            request
        }
        val body = BatchUpdateSpreadsheetRequest()
        body.requests = requests
        runBatchUpdate(body)
    }

    @Suppress("DuplicatedCode")
    fun addAlternatingColorsAndBoldTextToTab(tabName: String) {
        //alternating colors
        val gridRange = GridRange()
        val tabId = getSheetIdForTab(tabName)
        gridRange.sheetId = tabId
        gridRange.startRowIndex = 0

        val bandingProperties = BandingProperties()
        bandingProperties.headerColor = getHeaderColor()
        bandingProperties.firstBandColor = getFirstBandColor()
        bandingProperties.secondBandColor = getSecondBandColor()

        val bandedRange = BandedRange()
        bandedRange.range = gridRange
        bandedRange.rowProperties = bandingProperties

        val addBandingRequest = AddBandingRequest()
        addBandingRequest.bandedRange = bandedRange

        val bandingRequest = Request()
        bandingRequest.addBanding = addBandingRequest

        //bold
        val textFormat = TextFormat()
        textFormat.bold = true

        val cellFormat = CellFormat()
        cellFormat.textFormat = textFormat

        val textFormatGridRange = GridRange()
        textFormatGridRange.sheetId = tabId
        textFormatGridRange.startRowIndex = 0

        val cell = CellData()
        cell.userEnteredFormat = cellFormat

        val repeatCellRequest = RepeatCellRequest()
        repeatCellRequest.range = textFormatGridRange
        repeatCellRequest.cell = cell
        repeatCellRequest.fields = "userEnteredFormat(textFormat)"

        val formatText = Request()
        formatText.repeatCell = repeatCellRequest

        val body = BatchUpdateSpreadsheetRequest()
        body.requests = listOf(formatText, bandingRequest)
        runBatchUpdate(body)
    }

    private fun runBatchUpdate(batchUpdate: BatchUpdateSpreadsheetRequest) {
        retry {
            buildSheetsWithCredentials().spreadsheets().batchUpdate(googleSheetId(), batchUpdate)
                .execute()
        }
    }

    private fun getHeaderColor(): Color {
        val color = Color()
        color.red = 0.69f
        color.green = 0.69f
        color.blue = 0.69f
        return color
    }

    private fun getFirstBandColor(): Color {
        val color = Color()
        color.red = 1.0f
        color.green = 1.0f
        color.blue = 1.0f
        return color
    }

    private fun getSecondBandColor(): Color {
        val color = Color()
        color.red = 0.87f
        color.green = 0.87f
        color.blue = 0.87f
        return color
    }

    private fun getSheetIdForTab(tabName: String): Int {
        return getIdAndSheetPropertiesForTab(tabName).first
    }

    private fun buildSheetsWithCredentials(): Sheets {
        val key= googleAuthJson

        try {
            val credential = GoogleCredential
                .fromStream(key.byteInputStream())
                .createScoped(googleSheetScopes())

            return Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                credential)
                .setApplicationName(googleAppName)
                .build()
        } catch (e: Exception) {
            log.info("Exception trying to use oauth2 key.  Prefix: ${key.removeRange(10, key.length-1)}")
            throw e
        }
    }

    private fun googleSheetScopes(): List<String> {
        return listOf(SheetsScopes.SPREADSHEETS)
    }

    private fun getIdAndSheetPropertiesForTab(tabName: String): Pair<Int, SheetProperties> {
        return retry {
            val idAndPropertiesByTabName =
                buildSheetsWithCredentials().spreadsheets().get(googleSheetId())
                    .execute().sheets.associate {
                        val id = it.properties.sheetId
                        val title = it.properties.title
                        Pair(title, Pair(id, it.properties))
                    }
            return@retry idAndPropertiesByTabName[tabName]!!
        }
    }

    fun <T> reportNonHeaderRowsFromTab(tabName: String, rowMapper: (List<String>) -> T): List<T> {
        val rows = listValuesInTab(tabName)
        return rows.subList(1, rows.size) //remove header
            .map { row -> rowMapper.invoke(row) }
    }

    @Suppress("DuplicatedCode")
    fun saveAll(tabName: String, header: List<String>, rows: List<List<String>>, columnCount: Int) {
        retry {
            val values = mutableListOf(header)
            values.addAll(rows)

            @Suppress("UNCHECKED_CAST")
            val appendBody = ValueRange().setValues(values as List<MutableList<Any>>?)

            val range = "$tabName!A1"
            buildSheetsWithCredentials().spreadsheets().values()
                .append(googleSheetId(), range, appendBody)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute()
        }

        val dimensionRange = DimensionRange()
        dimensionRange.sheetId = getSheetIdForTab(tabName)
        dimensionRange.dimension = "COLUMNS"
        dimensionRange.startIndex = 0
        dimensionRange.endIndex = columnCount
        val autoResize = AutoResizeDimensionsRequest()
        autoResize.dimensions = dimensionRange
        val request = Request()
        request.autoResizeDimensions = autoResize
        val batchUpdateSpreadsheetRequest = BatchUpdateSpreadsheetRequest()
        batchUpdateSpreadsheetRequest.requests = listOf(request)
        runBatchUpdate(batchUpdateSpreadsheetRequest)
    }

    fun saveAllRowsSelfFormatted(tabName: String, rows: List<List<String>>) {
        retry {
            val appendBody = ValueRange().setValues(rows)
            val range = "$tabName!A1"
            buildSheetsWithCredentials().spreadsheets().values()
                .append(googleSheetId(), range, appendBody)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute()
        }
    }

    fun clearAllDataInTab(tabName: String) {
        retry {
            val clearValuesRequest = ClearValuesRequest()
            buildSheetsWithCredentials().spreadsheets().values().clear(
                googleSheetId(),
                tabName,
                clearValuesRequest
            ).execute()
        }
    }

    private fun <T> retry(fn: () -> T): T {
        var counter = 1
        var ex: Exception? = null
        while (counter <= 3) {
            try {
                return fn.invoke()
            } catch (e: Exception) {
                if (e is GoogleJsonResponseException) {
                    if (e.statusCode == 400) {
                        throw e
                    }
                }
                ex = e
                counter += 1
            }
        }
        if (ex != null) {
            throw ex
        } else {
            throw RuntimeException("This should never happen")
        }
    }

    private fun listValuesInTab(tabName: String): List<List<String>> {
        return retry {
            return@retry listValuesInTab(tabName, googleSheetId())
        }
    }

    private fun listValuesInTab(tabName: String, googleSheetId: String): List<List<String>> {
        val response = buildSheetsWithCredentials()
            .spreadsheets()
            .values()[googleSheetId, tabName]
            .execute()

        @Suppress("UNCHECKED_CAST")
        return response.values.toList()[2] as List<List<String>>
    }

    private fun googleSheetId(): String {
        return googleSheetId
    }
}