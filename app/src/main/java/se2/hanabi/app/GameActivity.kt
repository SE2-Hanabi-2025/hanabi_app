package se2.hanabi.app

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
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
        var selectedCardIndex by remember { mutableStateOf<Int?>(null) } // Track selected card
        var discardedCard by remember { mutableStateOf("No card discarded yet") }
        val drawnCards = remember { mutableStateListOf<String>() }
        val boxes = remember {
            mutableStateListOf<String?>(
                null,
                null,
                null,
                null,
                null
            )
        } // 5 boxes, each holding a card or null
        val coroutineScope = rememberCoroutineScope()
        val client = remember { HttpClient(CIO) }
        val urlEmulator = "http://10.0.2.2:8080"
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        var deckSize by remember { mutableStateOf(50)} //50 card deck

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
                        deckSize-- // Decrease deck size
                    }
                } catch (e: Exception) {
                    drawnCard = "Failed to draw a card"
                }
            }
        }

        fun discardCard() {
            if (selectedCardIndex != null) {
                drawnCards.removeAt(selectedCardIndex!!) // Remove the selected card from the hand
                discardedCard = "Discarded card: $selectedCardIndex"
                selectedCardIndex = null // Reset the selected card after discarding
            } else {
                discardedCard = "No card selected to discard" // Notify user if no card is selected
            }
        }

        // Function to remove a card from the hand (drawnCards list)
        fun removeCard(card: String) {
            drawnCards.remove(card) // Remove card from the drawnCards list
        }

        fun placeCardInBox(boxIndex: Int) {
            val selectedCard = selectedCardIndex?.let { drawnCards[it] }
            val cardValue = selectedCard?.toIntOrNull()
            val boxValue = boxes[boxIndex]?.toIntOrNull()

            if (cardValue != null) {
                if (boxValue == 5) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(("This stack is already complete!"))
                    }
                    return
                }

                val canPlace = when (boxValue) {
                    null -> cardValue == 1 //Only 1 can start a stack
                    else -> cardValue == boxValue + 1 //Stacking rule: next must be + 1
                }
                if (canPlace) {
                    boxes[boxIndex] = cardValue.toString()
                    drawnCards.removeAt(selectedCardIndex!!)
                    selectedCardIndex = null

                    if(cardValue == 5) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("\uD83C\uDF89 Stack ${boxIndex + 1} completed!")
                        }
                    }

                    //WIN CHECK
                    if(boxes.all { it == "5"}) {
                        context.startActivity(Intent(context, WinActivity::class.java))
                    }

                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Invalid move! You must play ${boxValue?.plus(1) ?: 1}!"
                        )
                    }
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
                    Text(
                        text = "Cards Remaining: $deckSize",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black
                    )

                    if (deckSize == 0) {
                        Text(
                            text = "No more cards left in the deck.",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Button(
                        onClick = { drawCard() },
                        enabled = deckSize > 0, //Disable when deck is empty
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
                    drawnCards.withIndex().chunked(5).reversed().forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            chunk.forEach { (index, card) ->
                                Button(
                                    onClick = {
                                        // Set the selected card when clicked
                                        selectedCardIndex = if (selectedCardIndex == index) null else index
                                    },
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .wrapContentSize(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedCardIndex == index) Color.Green else Color.Magenta // For background color
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
