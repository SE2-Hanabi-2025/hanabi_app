package se2.hanabi.app.Services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import se2.hanabi.app.model.Card
import se2.hanabi.app.model.GameStatus
import se2.hanabi.app.model.Hint

class GamePlayService(
    private val lobbyId: Int,
    private val playerId: Int
    ) {
    private val baseURL = "http://10.145.212.9:8080" // "http://10.0.2.2:8080" // for emulator
    private val client = HttpClient(CIO)

    suspend fun getGameStatus(): GameStatus? {
        try {
            val response: HttpResponse = client.get("$baseURL/{$lobbyId}/status")
            val gameStatus: GameStatus = Json.decodeFromString<GameStatus>(response.body())

            println("Received Game State: $gameStatus")
            println("Current Player: ${gameStatus.currentPlayer}")
            println("Number of Players: ${gameStatus.players.size}")

            return gameStatus
        } catch (e: Exception) {
            println("Error updating game status: ${e.message}")
        }
        return null
    }

    suspend fun  playCard(cardId: Int) {
        try {
            val response: HttpResponse = client.post("$baseURL/$lobbyId/play") {
                parameter("playerId", playerId)
                parameter("cardId", cardId)
            }

            if (response.status.isSuccess()) {
                println("Card successfully played")
            } else if (response.status == HttpStatusCode.BadRequest) {
                println("Invalid move")
            } else if (response.status == HttpStatusCode.NotFound) {
                println("Game or lobby not found")
            } else {
                println("Error playing card: ${response.status}")
            }

        } catch (e: Exception) {
            println("Error playing card: ${e.message}")
        }
    }

    suspend fun  drawCard() {
        try {
            val response: HttpResponse = client.post("$baseURL/{$lobbyId}/draw") {
                parameter("playerId", playerId)
            }

            if (response.status.isSuccess()) {
                println("Card successfully drawn")
            } else if (response.status == HttpStatusCode.BadRequest) {
                println("Invalid move: not your turn/ hand is full")
            } else if (response.status == HttpStatusCode.NotFound) {
                println("Game or lobby not found")
            } else {
                println("Error drawing card: ${response.status}")
            }

        } catch (e: Exception) {
            println("Error drawing card: ${e.message}")
        }
    }

    suspend fun  discardCard(cardId: Int) {
        try {
            val response: HttpResponse = client.post("$baseURL/{$lobbyId}/discard") {
                parameter("playerId", playerId)
                parameter("cardId", cardId)
            }

            if (response.status.isSuccess()) {
                println("Card successfully discarded")
            } else if (response.status == HttpStatusCode.BadRequest) {
                println("Invalid move: not your turn")
            } else if (response.status == HttpStatusCode.NotFound) {
                println("Game or lobby not found")
            } else {
                println("Error discarding card: ${response.status}")
            }

        } catch (e: Exception) {
            println("Error discarding card: ${e.message}")
        }
    }

    //hint in backend split into hintType and value
    suspend fun  giveHint(toPlayerId:Int, hint: Hint) {
        try {
            val response: HttpResponse = client.post("$baseURL/{$lobbyId}/hint") {
                parameter("fromPlayerId", playerId)
                parameter("toPlayerId", toPlayerId)
                parameter("hint", hint)
            }

            if (response.status.isSuccess()) {
                println("Hint successfully given")
            } else if (response.status == HttpStatusCode.BadRequest) {
                println("Invalid move: not a valid hint")
            } else if (response.status == HttpStatusCode.NotFound) {
                println("Game or lobby not found")
            } else {
                println("Error giving hint: ${response.status}")
            }

        } catch (e: Exception) {
            println("Error giving hint: ${e.message}")
        }
    }

}