package se2.hanabi.app.Services

import WebSocketService
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
    private val baseURL = "http://10.0.2.2:8080" // "http://10.0.2.2:8080" //"http://10.145.212.9:8080" for emulator
    private val client = HttpClient(CIO)
    private val webSocketService = WebSocketService()

    suspend fun getGameStatus(): GameStatus? {
        val msg = "LobbyId: $lobbyId"
        try {
            val response: HttpResponse = client.get("$baseURL/{$lobbyId}/status") {
                parameter("lobbyId", lobbyId)
            }
            getGameStatusSocket()
            if (response.status.isSuccess()) {
                val gameStatus: GameStatus = Json.decodeFromString<GameStatus>(response.body())
                println("Game status successfully updated | $msg")
                return gameStatus
            } else if (response.status == HttpStatusCode.BadRequest) {
                println("Unable to update game status  | $msg")
            } else if (response.status == HttpStatusCode.NotFound) {
                println("Game: $lobbyId not found")
            } else {
                println("Error updating game status | $msg: ${response.status} ")
            }

        } catch (e: Exception) {
            println("Error updating game status | $msg: ${e.message}")
        }
        return null
    }

    suspend fun  playCard(cardId: Int, stackColor: Card.Color) {
        val msg = "playerId: $playerId | cardId: $cardId | stackColor: $stackColor"
        try {
            val response: HttpResponse = client.post("$baseURL/$lobbyId/play") {
                parameter("playerId", playerId)
                parameter("cardId", cardId)
                parameter("color", stackColor)
            }
            playCardSocket(cardId,stackColor)
            if (response.status.isSuccess()) {
                println("Card successfully played | $msg")
            } else if (response.status == HttpStatusCode.BadRequest) {
                println("Invalid move | $msg")
            } else if (response.status == HttpStatusCode.NotFound) {
                println("Game: $lobbyId not found")
            } else {
                println("Error playing card | $msg: ${response.status} ")
            }

        } catch (e: Exception) {
            println("Error playing card | $msg: ${e.message}")
        }
    }

    suspend fun  discardCard(cardId: Int) {
        val msg = "playerId: $playerId | cardId: $cardId"
        try {
            val response: HttpResponse = client.post("$baseURL/{$lobbyId}/discard") {
                parameter("playerId", playerId)
                parameter("cardId", cardId)
            }
            discardCardSocket(cardId)
            if (response.status.isSuccess()) {
                println("Card successfully discarded | $msg")
            } else if (response.status == HttpStatusCode.BadRequest) {
                println("Invalid move: not your turn | $msg")
            } else if (response.status == HttpStatusCode.NotFound) {
                println("Game: $lobbyId not found")
            } else {
                println("Error discarding card | $msg: ${response.status} ")
            }

        } catch (e: Exception) {
            println("Error discarding card | $msg: ${e.message}")
        }
    }

    //hint in backend split into hintType and value
    suspend fun  giveHint(toPlayerId:Int, hint: Hint) {
        val msg = "fromPlayerId: $playerId | toPlayerId: $toPlayerId | hint: $hint"
        try {
            val response: HttpResponse = client.post("$baseURL/{$lobbyId}/hint") {
                parameter("fromPlayerId", playerId)
                parameter("toPlayerId", toPlayerId)
                parameter("hint", hint)
            }
            giveHintSocket(toPlayerId,hint)
            if (response.status.isSuccess()) {
                println("Hint successfully given | $msg")
            } else if (response.status == HttpStatusCode.BadRequest) {
                println("Invalid move: not a valid hint | $msg")
            } else if (response.status == HttpStatusCode.NotFound) {
                println("Game: $lobbyId not found")
            } else {
                println("Error giving hint | $msg: ${response.status}")
            }

        } catch (e: Exception) {
            println("Error giving hint | $msg: ${e.message}")
        }
    }

    suspend fun getGameStatusSocket() {
        webSocketService.sendMessage("Request through socket: getGameStatus for lobby $lobbyId")
    }

    suspend fun playCardSocket(cardId: Int, stackColor: Card.Color) {
        webSocketService.sendMessage("Request through socket: playCard $cardId of color $stackColor by player $playerId in lobby $lobbyId")
    }

    suspend fun discardCardSocket(cardId: Int) {
        webSocketService.sendMessage("Request through socket: discardCard $cardId by player $playerId in lobby $lobbyId")
    }

    suspend fun giveHintSocket(toPlayerId: Int, hint: Hint) {
        webSocketService.sendMessage("Request through socket: giveHint $hint from $playerId to $toPlayerId in lobby $lobbyId")
    }

}