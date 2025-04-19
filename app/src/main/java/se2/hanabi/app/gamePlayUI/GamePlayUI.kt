package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import se2.hanabi.app.card.Card
import se2.hanabi.app.screens.Title
import kotlin.random.Random

// eventually to be linked to Color Enum in backend
val colors = listOf("red","green","yellow","blue","white")

/**
 * GamePlayUI displays screen that will be active in gameplay.
 * This includes:
 * - the cards from all players' hands
 * - fuse/hint tokens, discard/draw pile, color stacks
 * - game title at top
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
        val titleModifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 20.dp)
            .clickable(
//                        interactionSource = remember { MutableInteractionSource() },
//                        indication = null
            ) {
                // show win screen/overlay
            }
        Title(modifier = titleModifier)
        PlayersCardsUI(generateTestHands(5))
        GameBoardUI(
            stackValues = generateTestColorStackValues(),
            numRemainingCards = Random.nextInt(35),
            lastDiscardedCard = randomCard(),
            numRemainingHintTokens = Random.nextInt(9),
            numRemainingFuseTokens = Random.nextInt(4),
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