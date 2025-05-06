package se2.hanabi.app.Services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json
import se2.hanabi.app.Model.GameStatus
import se2.hanabi.app.Model.Hint

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

    //TODO fill out stub
    suspend fun  playCard(cardId: Int) {
        try {
            val response: HttpResponse = client.post("$baseURL/{$lobbyId}/play")

        } catch (e: Exception) {
            println("Error playing card: ${e.message}")
        }
    }

    //TODO fill out stub
    suspend fun  drawCard() {
        try {
            val response: HttpResponse = client.post("$baseURL/{$lobbyId}/draw")

        } catch (e: Exception) {
            println("Error drawing card: ${e.message}")
        }
    }

    //TODO fill out stub
    suspend fun  discardCard(cardId: Int) {
        try {
            val response: HttpResponse = client.post("$baseURL/{$lobbyId}/discard")

        } catch (e: Exception) {
            println("Error discarding card: ${e.message}")
        }
    }

    //TODO fill out stub //hint in backend split into hintType and value
    suspend fun  giveHint(toPlayer:Int, hint: Hint) {
        try {
            val response: HttpResponse = client.post("$baseURL/{$lobbyId}/hint")

        } catch (e: Exception) {
            println("Error giving hint: ${e.message}")
        }
    }

}