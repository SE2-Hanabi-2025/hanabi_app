package se2.hanabi.app.Services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json
import se2.hanabi.app.Model.GameStatus

class GamePlayService() {
    private val baseURL = "http://10.145.212.9:8080" // "http://10.0.2.2:8080" // for emulator
    private val client = HttpClient(CIO)

    suspend fun updateGameStatus() {
        try {
            val response: HttpResponse = client.get("$baseURL/game/state")
            val gameState: GameStatus = Json.decodeFromString<GameStatus>(response.body())

            println("Received Game State: $gameState")
            println("Current Player: ${gameState.currentPlayer}")
            println("Number of Players: ${gameState.players.size}")

        } catch (e: Exception) {
            println("Error updating game status: ${e.message}")
        }
    }
}