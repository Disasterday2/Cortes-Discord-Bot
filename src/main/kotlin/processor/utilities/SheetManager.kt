package processor.utilities

import ConfigReader
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsRequestInitializer
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import java.io.FileNotFoundException

object SheetManager {

    val sheetsService: Sheets
    const val SPREADSHEETID = "10cFx0Ti3UniuZWKHEan-wW21JOUSIWJOQhkEcaI4QSE"
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS, SheetsScopes.SPREADSHEETS_READONLY)

    init {
        val client: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        val jsonReader = JacksonFactory.getDefaultInstance()

        val inputStream = SheetManager.javaClass.getResourceAsStream("/client_secret.json")
            ?: throw FileNotFoundException("Missing client_credentials!")

        val clientSecrets =
            GoogleCredential.fromStream(
                inputStream, client, jsonReader
            ).createScoped(SCOPES)

        sheetsService = Sheets.Builder(client, jsonReader, clientSecrets)
            .setSheetsRequestInitializer(SheetsRequestInitializer(ConfigReader().readConfig().sheetsToken))
            .setApplicationName("TheTemple Spreadsheet")
            .build()
    }


    fun test() {
        println("in here");
        val properties = SheetProperties()
        properties.title = "dickard"
        val dickard = sheetsService.spreadsheets().batchUpdate(
            SPREADSHEETID, BatchUpdateSpreadsheetRequest().setRequests(
                listOf(Request().setAddSheet(AddSheetRequest().setProperties(properties)))
            )
        ).execute()
        println(dickard)
    }
}