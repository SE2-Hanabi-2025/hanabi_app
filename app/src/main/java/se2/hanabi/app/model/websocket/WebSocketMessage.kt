package se2.hanabi.app.model.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import se2.hanabi.app.model.Card
import se2.hanabi.app.model.GameStatus

/**
 * Base class for all WebSocket messages
 */
@Serializable
sealed class WebSocketMessage

/**
 * Client-to-Server Messages
 */
@Serializable
sealed class ClientAction : WebSocketMessage() {
    abstract val action: String
    abstract val lobbyId: String
    abstract val playerId: Int
}

@Serializable
@SerialName("PLAY")
data class PlayCardAction(
    override val lobbyId: String,
    override val playerId: Int,
    val cardIndex: Int
) : ClientAction() {
    override val action: String = "PLAY"
}

@Serializable
@SerialName("DISCARD")
data class DiscardCardAction(
    override val lobbyId: String,
    override val playerId: Int,
    val cardIndex: Int
) : ClientAction() {
    override val action: String = "DISCARD"
}

@Serializable
@SerialName("HINT")
data class GiveHintAction(
    override val lobbyId: String,
    override val playerId: Int,
    val toPlayerId: Int,
    val hintType: HintType,
    val hintValue: String
) : ClientAction() {
    override val action: String = "HINT"
}

@Serializable
@SerialName("DEFUSE_ATTEMPT")
data class DefuseAttemptAction(
    override val lobbyId: String,
    override val playerId: Int,
    val sequence: List<String>, // e.g. ["down", "down", "up", "down"]
    val proximity: String // e.g. "dark" or "light"
) : ClientAction() {
    override val action: String = "DEFUSE_ATTEMPT"
}

@Serializable
enum class HintType {
    COLOR,
    VALUE;
    
    override fun toString(): String {
        return name
    }
}

/**
 * Server-to-Client Messages
 */
@Serializable
sealed class ServerResponse : WebSocketMessage()

@Serializable
data class ActionResult(
    val type: ResultType,
    val message: String,
    val details: ActionDetails? = null
) : ServerResponse()

@Serializable
enum class ResultType {
    SUCCESS,
    FAILURE,
    INVALID_MOVE,
    ERROR
}

@Serializable
data class ActionDetails(
    val cardPlayed: Card? = null,
    val newCard: Card? = null
)

@Serializable
data class ErrorResponse(
    val error: String
) : ServerResponse()

@Serializable
data class GameStateUpdate(
    val gameState: GameStatus
) : ServerResponse()
