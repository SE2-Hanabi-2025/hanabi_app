package se2.hanabi.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.launch
import se2.hanabi.app.gamePlayUI.GamePlayUI
import se2.hanabi.app.logic.GameManager
import se2.hanabi.app.logic.PlaceCardResult

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // UI will be developed on GamePlayUI
            // GamePlayUI will eventually replace GameScreen
            GamePlayUI()
            // GameScreen()
        }
    }

    @Composable
    fun GameScreen() {
        val gameManager = remember { GameManager() }
        val coroutineScope = rememberCoroutineScope()
        val client = remember { HttpClient(CIO) }
        val urlEmulator = "http://10.0.2.2:8080"
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current

        fun drawCard() {
            coroutineScope.launch {
                try {
                    val response: HttpResponse = client.get("$urlEmulator/game/draw")
                    val cardText = response.body<String>()

                    val cardValue = cardText.substringAfter(": ").trim()
                    gameManager.drawCard(cardValue)

                } catch (e: Exception) {

                }
            }
        }

        fun discardCard() {
            gameManager.discardSelectedCard()
        }

        fun placeCardInBox(index: Int) {
            when (val result = gameManager.placeCardInBox(index)) {
                is PlaceCardResult.Valid -> {}
                is PlaceCardResult.StackCompleted -> coroutineScope.launch {
                    snackbarHostState.showSnackbar("\uD83C\uDF89 Stack ${index + 1} completed!")
                }
                is PlaceCardResult.GameWon -> context.startActivity(Intent(context, WinActivity::class.java))
                is PlaceCardResult.StackFull -> coroutineScope.launch {
                    snackbarHostState.showSnackbar("This stack is already complete!")
                }
                is PlaceCardResult.InvalidMove -> coroutineScope.launch {
                    snackbarHostState.showSnackbar("Invalid move! You must play ${result.expected}!")
                }
                is PlaceCardResult.InvalidSelection -> coroutineScope.launch {
                    snackbarHostState.showSnackbar("No card selected.")
                }
            }
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    val isSuccess = data.visuals.message.startsWith("\uD83C\uDF89") || data.visuals.message.contains("completed", ignoreCase = true)

                    Snackbar(
                        snackbarData = data,
                        containerColor = if (isSuccess) Color(0xFF4CAF50) else Color (0xFFF44336), //green or red
                        contentColor = Color.White
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text("Cards Remaining: ${gameManager.deckSize}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black)

                    if (gameManager.deckSize == 0) {
                        Text(
                            text = "No more cards left in the deck.",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Button(
                        onClick = { drawCard() },
                        enabled = gameManager.deckSize > 0,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("Draw Card")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = gameManager.lastDrawnMessage, style = MaterialTheme.typography.headlineSmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Discard Card Button
                    Button(
                        onClick = { discardCard() },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("Discard Card")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = gameManager.lastDiscardedMessage, style = MaterialTheme.typography.headlineSmall)
                }

                // Playing field with 5 boxes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        Box(
                            modifier = Modifier
                                .size(50.dp) // Size of each box
                                .background(
                                    if (gameManager.boxes [index] != null) Color.Magenta else Color.Gray
                                ) // Color purple if it has a card, else gray
                                .border(1.dp, Color.Gray) // Border around each box
                                .padding(8.dp)
                                .clickable {
                                    placeCardInBox(index) // Place card when box is clicked
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gameManager.boxes[index] ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp)) // Space between boxes
                    }
                }

                // Drawn Cards display
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    gameManager.drawnCards.withIndex().chunked(5).reversed().forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            chunk.forEach { (index, card) ->
                                Button(
                                    onClick = {
                                         gameManager.selectCard(index)
                                    },
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .wrapContentSize(),
                                    colors = ButtonDefaults.buttonColors(
                                         // For background color
                                        containerColor = if (gameManager.selectedCardIndex == index) Color.Green else Color.Magenta
                                    )
                                ) {
                                    Text(
                                        card,
                                        modifier = Modifier.padding(4.dp),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
