package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.Services.WebSocketService
import se2.hanabi.app.model.Player
import se2.hanabi.app.R

// eventually to be linked to Color Enum in backend
val colors = listOf("red","green","yellow","blue","white")

/**
 * GamePlayUI displays screen that will be active in gameplay.
 * This includes:
 * - the cards from all players' hands
 * - fuse/hint tokens, discard/draw pile, color stacks
 *
 */

@Composable
fun GamePlayUI() {
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
            Column (modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally){
            // Show game status overlay at the top
            Column(
                modifier = Modifier
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

            // Show game board and player cards

                Spacer(modifier = Modifier.height(12.dp))

                InGamePlayerList(players = players)}
                GameBoardUI()
                PlayersCardsUI()


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

@Composable
fun InGamePlayerList(players: List<Player>, modifier: Modifier = Modifier){
    Column (
        modifier = Modifier.padding(top = 18.dp, bottom = 9.dp)
            .fillMaxWidth(0.5f)
            .heightIn(max = 200.dp)
            .background(Color.DarkGray.copy(alpha = 0.20f), RoundedCornerShape(40.dp))
            .padding(horizontal = 30.dp, vertical = 25.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        players.forEach{player ->
        Row (
            modifier = Modifier
                .fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start) {

            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.DarkGray)){
                Image(painter = painterResource(id = player.avatarResID),
                    contentDescription = "${player.name} avatar",
                    modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(text = player.name,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f))
        }}
    }
}
