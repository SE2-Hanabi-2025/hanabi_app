# Hanabi Game WebSocket Implementation

This document describes how the WebSocket API has been implemented in the Hanabi game application.

## Overview

The WebSocket implementation allows for real-time gameplay actions like playing cards, discarding cards, and giving hints. The communication between the client and server follows the specified API protocol.

## Components

### 1. WebSocketService

The `WebSocketService` class handles the core WebSocket connection and communication:

- Manages WebSocket connection lifecycle
- Provides methods for game actions (play, discard, hint)
- Handles serialization and deserialization of WebSocket messages
- Exposes state flows for game state and action results

### 2. WebSocket Message Models

The message models in `WebSocketMessage.kt` define the structure for communication between client and server:

- `ClientAction` - Base class for all client actions
  - `PlayCardAction` - Action to play a card
  - `DiscardCardAction` - Action to discard a card
  - `GiveHintAction` - Action to give a hint

- `ServerResponse` - Base class for all server responses
  - `ActionResult` - Result of a game action
  - `GameStateUpdate` - Updated game state
  - `ErrorResponse` - Error message

### 3. GamePlayViewModel

The `GamePlayViewModel` uses the WebSocketService to:

- Maintain connection to the game server
- Send player actions to the server
- Update the UI based on game state changes
- Handle reconnection logic

## Usage Example

### Connecting to the Game Server

```kotlin
// In GamePlayViewModel
private val webSocketService = WebSocketService()

fun connectToWebSocket() {
    webSocketService.connect(lobbyId, playerId)
}
```

### Sending Actions

```kotlin
// Play a card
viewModelScope.launch {
    webSocketService.playCard(lobbyId, playerId, cardIndex)
}

// Discard a card
viewModelScope.launch {
    webSocketService.discardCard(lobbyId, playerId, cardIndex)
}

// Give a hint
viewModelScope.launch {
    webSocketService.giveHint(lobbyId, playerId, toPlayerId, hint)
}
```

### Receiving Updates

```kotlin
// In GamePlayViewModel.setupWebSocketListeners()
viewModelScope.launch {
    // Listen for game state updates
    webSocketService.gameState.collect { newGameState ->
        newGameState?.let {
            updateGameStatus(it)
        }
    }
}

viewModelScope.launch {
    // Listen for action results
    webSocketService.actionResult.collect { result ->
        // Handle result
    }
}
```

## Connection Parameters

The WebSocket connection URL follows this format:

```
ws://{server-address}/ws/game?lobbyId={lobbyId}&playerId={playerId}
```

## Message Format Examples

### Client-to-Server: Play a Card

```json
{
  "action": "PLAY",
  "lobbyId": "LOBBY123",
  "playerId": 1,
  "cardIndex": 2
}
```

### Server-to-Client: Action Result

```json
{
  "type": "SUCCESS",
  "message": "Card played successfully",
  "details": {
    "cardPlayed": {
      "color": "BLUE",
      "value": 3
    },
    "newCard": {
      "color": "RED",
      "value": 1
    }
  }
}
```

## Error Handling

The WebSocket service includes comprehensive error handling:

- Connection errors
- Message parsing errors
- Invalid action errors
- Automatic reconnection attempts

## Testing

A sample `WebSocketClient` class is provided for testing purposes, demonstrating how to use the WebSocketService in different scenarios.
