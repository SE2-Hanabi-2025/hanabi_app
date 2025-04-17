package se2.hanabi.app.gameBoardUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import se2.hanabi.app.card.Card
import se2.hanabi.app.card.CardItem

@Composable
fun GameBoard() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            Brush.verticalGradient(
                listOf(Color(0xFF282828), Color(0xFF000000))
            )
        )
    ) {
        val testHand = listOf(
            Card(color = "Red", number = 5),
            Card(color = "Blue", number = 3),
            Card(color = "Green", number = 4),
            Card(color = "Yellow", number = 2),
//            Card(color = "White", number = 1)
        )

        PlayersHand(testHand)
    }
}

@Composable
fun PlayersHand(hand: List<Card>) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            Arrangement.SpaceEvenly,
        ) {
            hand.forEach { card ->
                CardItem(
                    card = card,
                    flipCardState = false  // frontside by default
                )
            }
        }
    }
}