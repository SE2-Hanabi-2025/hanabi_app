package se2.hanabi.app.gamePlayUI

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.Services.WebSocketService

// eventually to be linked to Color Enum in backend
val colors = listOf("red","green","yellow","blue","white")
const val maxGameBoardHeightProportion = 0.5f

/**
 * GamePlayUI displays screen that will be active in gameplay.
 * This includes:
 * - the cards from all players' hands
 * - fuse/hint tokens, discard/draw pile, color stacks
 *
 */
@Composable
fun GamePlayUI() {
    val configuration = LocalConfiguration.current
    var screeWidthDp = configuration.screenWidthDp.dp
    var screenHeightDP = configuration.screenHeightDp.dp


    val landscape = when (configuration.orientation) { Configuration.ORIENTATION_LANDSCAPE -> true else -> false }

    var shrinkRatio = 1f
    var defaultCardWidth = screeWidthDp.times(cardProportionOfWidth)
    var cardWidth: Dp

    if (landscape) {
        val gameBoardVertPaddingElementsSum = boardElementPadding.times(4)
        val availableVertSpace = screenHeightDP.times(maxGameBoardHeightProportion)-gameBoardVertPaddingElementsSum
        val cardHeight = availableVertSpace.div(2)
        cardWidth = cardHeight.div(aspectRatio)

    } else { // portrait
        cardWidth = screeWidthDp.times(cardProportionOfWidth)
        val gameBoardVertPaddingElementsSum = cardSpacing.times(4) + boardElementPadding.times(2)
        val gameBoardHeight = cardWidth.times(5) + gameBoardVertPaddingElementsSum
        if (gameBoardHeight.div(screenHeightDP) > maxGameBoardHeightProportion) {
            val availableVertSpace =
                screenHeightDP.times(maxGameBoardHeightProportion) - gameBoardVertPaddingElementsSum
            cardWidth = availableVertSpace.div(5)
        }
    }

    shrinkRatio = cardWidth.div(defaultCardWidth)

    val cardSizeDp = DpSize(
        width = cardWidth,
        height = cardWidth.times(aspectRatio)
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            Brush.verticalGradient(
                listOf(Color(0xFF282828), Color(0xFF000000))
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        val viewModel: GamePlayViewModel = viewModel()

        val players by viewModel.players.collectAsState()
        val statusMessage by viewModel.statusMessage.collectAsState()
        val connectionState by viewModel.connectionState.collectAsState()
        val isMyTurn by viewModel.isMyTurn.collectAsState()
        val gameOver by viewModel.gameOver.collectAsState()

        // Zeige Ladeanimation wenn noch keine Spieler geladen wurden
        if (players.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                statusMessage?.let {
                    Text(
                        text = it,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                // Show reconnect button if disconnected
                if (connectionState == WebSocketService.ConnectionState.DISCONNECTED) {
                    androidx.compose.material3.Button(
                        onClick = { viewModel.reconnectWebSocket() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Reconnect")
                    }
                }
            }
        } else {
            // Show game board and player cards
            GameBoardUI(cardSizeDp, shrinkRatio)
            val context = LocalContext.current
            val tiltSensor = remember { TiltCheatSensor(context) }
            val isTilted = tiltSensor.isTilted.value
            LaunchedEffect(Unit) { tiltSensor.start() }
            PlayersCardsUI(landscape, cardSizeDp, isCheatMode = isTilted)
            
            // Show game status overlay at the top
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(
                    text = when (connectionState) {
                        WebSocketService.ConnectionState.CONNECTED -> "Connected"
                        WebSocketService.ConnectionState.CONNECTING -> "Connecting..."
                        WebSocketService.ConnectionState.DISCONNECTED -> "Disconnected"
                    },
                    color = when (connectionState) {
                        WebSocketService.ConnectionState.CONNECTED -> Color.Green
                        WebSocketService.ConnectionState.CONNECTING -> Color.Yellow
                        WebSocketService.ConnectionState.DISCONNECTED -> Color.Red
                    }
                )
                
                Text(
                    text = if (isMyTurn) "Your turn" else "Waiting for other player",
                    color = if (isMyTurn) Color.Green else Color.White
                )
                
                statusMessage?.let {
                    Text(text = it, color = Color.White)
                }
                
                if (gameOver) {
                    Text(
                        text = "Game Over!",
                        color = Color.Red,
                        fontSize = 20.sp
                    )
                }
            }
            
            // Action buttons at the bottom
            if (isMyTurn && !gameOver) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    androidx.compose.material3.Button(
                        onClick = { viewModel.onPlayCardClick() },
                        enabled = viewModel.selectedCardId.collectAsState().value >= 0
                    ) {
                        Text("Play Card")
                    }
                    
                    androidx.compose.material3.Button(
                        onClick = { viewModel.onDiscardCardClick() },
                        enabled = viewModel.selectedCardId.collectAsState().value >= 0 && 
                                 viewModel.numRemainingHintTokens.collectAsState().value < 8
                    ) {
                        Text("Discard Card")
                    }
                }
            }
        }
    }
}
