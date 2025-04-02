package se2.hanabi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.launch

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameScreen()
        }
    }

    @Composable
    fun GameScreen() {
        var drawnCard by remember { mutableStateOf("No card drawn yet") }
        var selectedCard by remember { mutableStateOf<String?>(null) } // Track selected card
        var discardedCard by remember { mutableStateOf("No card discarded yet") }
        val drawnCards = remember { mutableStateListOf<String>() }
        val boxes = remember { mutableStateListOf<String?>(null, null, null, null, null) } // 5 boxes, each holding a card or null
        val coroutineScope = rememberCoroutineScope()
        val client = remember { HttpClient(CIO) }
        val urlEmulator = "http://10.0.2.2:8080"

        fun drawCard() {
            coroutineScope.launch {
                try {
                    val response: HttpResponse = client.get("$urlEmulator/game/draw")
                    val cardText = response.body<String>()

                    if (cardText.contains("No more cards in the deck")) {
                        drawnCard = "No more cards in the deck!"
                    } else {
                        val cardValue = cardText.substringAfter(": ").trim()
                        drawnCard = "Drew a card: $cardValue"
                        drawnCards.add(cardValue)
                    }
                } catch (e: Exception) {
                    drawnCard = "Failed to draw a card"
                }
            }
        }

        fun discardCard() {
            if (selectedCard != null) {
                drawnCards.remove(selectedCard) // Remove the selected card from the hand
                discardedCard = "Discarded card: $selectedCard"
                selectedCard = null // Reset the selected card after discarding
            } else {
                discardedCard = "No card selected to discard" // Notify user if no card is selected
            }
        }
        // Function to remove a card from the hand (drawnCards list)
        fun removeCard(card: String) {
            drawnCards.remove(card) // Remove card from the drawnCards list
        }
        fun placeCardInBox(boxIndex: Int) {
            val cardValue = selectedCard?.toIntOrNull()
            val boxValue = boxes[boxIndex]?.toIntOrNull()

            if (cardValue != null) {
                val canPlace = when (boxValue) {
                    null -> cardValue == 1 //Only 1 can start a stack
                    else -> cardValue == boxValue + 1 //Stacking rule: next must be + 1
                }
                if(canPlace) {
                    boxes[boxIndex] = cardValue.toString()
                    removeCard(selectedCard!!)
                    selectedCard = null
                }
            }

        }



        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { drawCard() },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text("Draw Card")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = drawnCard, style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(16.dp))

                // Discard Card Button
                Button(
                    onClick = { discardCard() },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text("Discard Card")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = discardedCard, style = MaterialTheme.typography.headlineSmall)
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
                                if (boxes[index] != null) Color.Magenta else Color.Gray
                            ) // Color purple if it has a card, else gray
                            .border(1.dp, Color.Gray) // Border around each box
                            .padding(8.dp)
                            .clickable {
                                placeCardInBox(index) // Place card when box is clicked
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = boxes[index] ?: "",
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
                for (row in drawnCards.chunked(5).reversed()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        row.forEach { card ->
                            Button(
                                onClick = {
                                    // Set the selected card when clicked
                                    selectedCard = if (selectedCard == card) null else card
                                },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .wrapContentSize(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (card == selectedCard) Color.Green else Color.Magenta // For background color
                                )
                            ) {
                                Text(card, modifier = Modifier.padding(4.dp), color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
