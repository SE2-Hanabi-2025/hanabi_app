package se2.hanabi.app.Services

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import se2.hanabi.app.model.GameStatus
import se2.hanabi.app.model.Hint
import se2.hanabi.app.model.HintType

/**
 * Vereinfachte Version des GamePlayService, die nur den initialen Spielstatus lädt
 * ohne WebSockets oder Spielaktionen
 */
class GamePlayService(
    private val lobbyId: String,
    private val playerId: Int
) {    companion object {
        private const val TAG = "HanabiGamePlayService"
    }

    private val baseURL = "http://10.0.2.2:8080/api/game"
    private val client = HttpClient(CIO)
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun getGameStatus(): GameStatus? {
        val msg = "LobbyId: $lobbyId"
        Log.d(TAG, "Fordere Spielstatus an: $msg")

        try {
            Log.d(TAG, "HTTP-Anfrage an $baseURL/$lobbyId/status")
            val response: HttpResponse = client.get("$baseURL/$lobbyId/status") {
                parameter("playerId", playerId)
            }

            Log.d(TAG, "HTTP-Status: ${response.status}")

            if (response.status.isSuccess()) {
                Log.d(TAG, "Erfolgreiche Antwort, dekodiere GameStatus")
                val responseBody = response.body<String>()
                Log.v(TAG, "Response Body: $responseBody")

                val gameStatus: GameStatus = jsonParser.decodeFromString<GameStatus>(responseBody)
                Log.d(TAG, "Spielstatus erfolgreich aktualisiert | $msg | ${gameStatus.players.size} Spieler")
                return gameStatus
            } else if (response.status == HttpStatusCode.BadRequest) {
                Log.e(TAG, "Ungültige Anfrage beim Aktualisieren des Spielstatus | $msg")
            } else if (response.status == HttpStatusCode.NotFound) {
                Log.e(TAG, "Spiel: $lobbyId nicht gefunden")
            } else {
                Log.e(TAG, "Fehler beim Aktualisieren des Spielstatus | $msg: ${response.status}")
            }
        } catch (e: kotlinx.serialization.SerializationException) {
            Log.e(TAG, "JSON-Deserialisierungsfehler beim Aktualisieren des Spielstatus | $msg", e)
            Log.e(TAG, "Bitte überprüfen Sie, ob die GameStatus-Klasse mit dem Server-Modell übereinstimmt")
        } catch (e: Exception) {
            Log.e(TAG, "Exception beim Aktualisieren des Spielstatus | $msg", e)
        }
        return null
    }
}
