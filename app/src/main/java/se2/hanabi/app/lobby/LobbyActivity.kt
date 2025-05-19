package se2.hanabi.app.lobby

import androidx.activity.viewModels
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import se2.hanabi.app.R
import androidx.compose.ui.unit.sp
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ktor.http.HttpStatusCode
import androidx.lifecycle.lifecycleScope
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import se2.hanabi.app.activities.GameActivity
import se2.hanabi.app.ui.theme.ClientTheme

class LobbyActivity : ComponentActivity() {

    private val viewModel: LobbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val receivedLobbyCode = intent.getStringExtra("lobbyCode") ?: "Kein Code"
        val isHost = intent.getBooleanExtra("isHost", false)
        val username = intent.getStringExtra("username") ?: ""

        viewModel.setLobbyCode(receivedLobbyCode)
        viewModel.setIsHost(isHost)
        viewModel.setUsername(username)
        viewModel.startPlayerSync()


        setContent {
            ClientTheme {
                val players by viewModel.players.collectAsState()
                val lobbyCode = viewModel.lobbyCode
                val isHostState = viewModel.isHost
                val isGameStarted by viewModel.isGameStarted.collectAsState()

                LaunchedEffect(isGameStarted) {
                    if (isGameStarted) {
                        // Assuming viewModel.getPlayerId() returns the current player's ID
                        // You might need to implement getPlayerId() in your LobbyViewModel
                        // or retrieve it in a way that makes sense for your app's logic.
                        // For now, let's assume a placeholder or a method to get it.
                        // If lobbyCode is null, it might indicate an issue, handle appropriately.
                        lobbyCode?.let { lc ->
                            val currentPlayerId = viewModel.getPlayerId() // Placeholder for actual player ID retrieval
                            navigateToGame(lc, currentPlayerId)
                        }
                    }
                }

                LobbyScreen(
                    playerList = players,
                    lobbyCode = lobbyCode,
                    isHost = isHostState,
                    onLeaveLobby = { finish() },
                    onStartGame = { lobbyCode?.let { startGameRequest(it) } },
                )
            }
        }
    }

    private fun navigateToGame(lobbyId: String, playerId: Int) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("lobbyId", lobbyId)
            putExtra("playerId", playerId)
        }
        startActivity(intent)
    }

    private fun startGameRequest(lobbyCode: String) {
        lifecycleScope.launch {
            try {
                val response: HttpResponse =
                    HttpClient(CIO).get("http://10.0.2.2:8080/start-game/$lobbyCode")
                if (response.status == HttpStatusCode.OK) {
                    // Assuming viewModel.getPlayerId() returns the current player's ID
                    val currentPlayerId = viewModel.getPlayerId() // Placeholder for actual player ID retrieval
                    navigateToGame(lobbyCode, currentPlayerId)
                }
            } catch (e: Exception) {
            }
        }
    }


    @Composable
    fun LobbyScreen(
        playerList: List<String>,
        lobbyCode: String?,
        onLeaveLobby: () -> Unit,
        onStartGame: () -> Unit,
        isHost: Boolean
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.lobbyscreen_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )


            //TODO: Implement server connection
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp)
                    .offset(x = 33.dp, y = 100.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            )
            {
                Text(
                    text = lobbyCode?.let { "Lobby Code: $it" } ?: "Loading...",
                    color = Color.White,
                    fontSize = 22.sp
                )
            }
            //Player list Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .heightIn(max = 400.dp)
                    .offset(y = 200.dp, x = 55.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 30.dp, vertical = 50.dp)
            )
            {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(playerList) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(30.dp)
                        )
                        {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray)
                            )

                            Text(
                                text = player,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            //Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 62.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                //start game
                if (isHost) {
                    Button(
                        onClick = { onStartGame() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2ecc71),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier.width(200.dp).height(60.dp)
                    ) {
                        Text("Start Game", color = Color.White, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
                //leave Lobby
                Button(
                    onClick = onLeaveLobby,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray
                    ),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.width(200.dp).height(60.dp)
                ) {
                    Text("Leave Lobby", color = Color.White, fontSize = 20.sp)
                }

            }
        }

    }
}