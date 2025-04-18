package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import se2.hanabi.app.card.Card
import kotlin.random.Random

// eventually to be linked to Color Enum in backend
val colors = listOf("red","green","yellow","blue","white")

/*
displays screen that will be active in gameplay.
This includes:
- all players' hands
- fuse/hint tokens, discard/draw pile, color stacks
- (?) game title at top
 */
@Composable
fun GamePlayUI() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            Brush.verticalGradient(
                listOf(Color(0xFF282828), Color(0xFF000000))
            )
        )
    ) {
        val hands = generateTestHands(5)
        PlayersCardsUI(hands)
        GameBoardUI(
            stackValues = generateTestColorStackValues(),
            numRemainingCards = Random.nextInt(35),
            lastDiscardedCard = randomCard()
        )
    }
}

fun randomCard(): Card {
    return Card(colors[Random.nextInt(colors.size)],Random.nextInt(5)+1)
}

fun generateTestColorStackValues(): IntArray {
    var values = IntArray(5)
    for (i in colors.indices) {
        values[i] = Random.nextInt(6)
    }
    return values
}

fun generateTestHands(numPlayers: Int): List<List<Card>> {
    val numPlayers = numPlayers.coerceIn(2,5)
    val hands = mutableListOf<List<Card>>()
    val handSize = if (numPlayers<=3) 5 else 4

    for (i in 0 until numPlayers) {
        hands.add(randomHand(handSize))
    }
    return hands
}

fun randomHand(numCards: Int): List<Card> {

    val hand = mutableListOf<Card>()
    for (i in 0 until numCards) {
        hand.add( Card( colors[Random.nextInt(colors.size)], Random.nextInt(4)+1))
    }
    return hand
}