package se2.hanabi.app.gameBoardUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import se2.hanabi.app.card.Card
import se2.hanabi.app.card.CardItem
import kotlin.math.roundToInt
import kotlin.random.Random

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
        PlayersHand(randomHand(4))
        OtherPlayersHands(listOf(randomHand(5),randomHand(4),randomHand(4),randomHand(5)))
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
                    flipCardState = true
                )
            }
        }
    }
}

@Composable
fun OtherPlayersHands(hands: List<List<Card>>) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { newSize -> boxSize = newSize }


    ) {

    var rotationAmountZ = remember { mutableFloatStateOf(0f) }
    var handOffsetX by remember { mutableStateOf(0f) }
    var handOffsetY by remember { mutableStateOf(0f) }

    hands.forEachIndexed() {index, hand ->
        if (index%2 ==0) {// right hand side of screen
            rotationAmountZ.floatValue = -90f
            handOffsetX = boxSize.width.toFloat()
        } else {
            rotationAmountZ.floatValue = 90f
            handOffsetX = 0f
        }

        handOffsetY = if (index>1) {// offset top two hands to be at third of the screen down form top
            boxSize.height*0.25f
        } else {
            boxSize.height*0.6f
        }
        val handOffset = Offset(handOffsetX, handOffsetY)
        OtherPlayersHand(handOffset, hand, rotationAmountZ.floatValue)
        }
    }


}

@Composable
fun OtherPlayersHand(
    offset: Offset,
    hand: List<Card>,
    rotationAmountZ: Float) {
    var rowSize by remember { mutableStateOf(IntSize.Zero) }
    Row(
        modifier = Modifier
            .wrapContentSize()
            .offset { IntOffset((offset.x-rowSize.width/2).roundToInt(), offset.y.roundToInt()) } //-rowSize.width/2).dp
            .graphicsLayer {
                rotationZ = rotationAmountZ
            }
            .onSizeChanged { newSize -> rowSize = newSize },
        horizontalArrangement = Arrangement.spacedBy((-45.dp)),
        verticalAlignment = Alignment.CenterVertically,

        ) {
        hand.forEachIndexed() { index, card ->
            CardItem(
                card = card,
                flipCardState = false,
                rotationAmountZ = -30f + index*(60/hand.size) //60 degree arc
            )
        }
    }
}

fun randomHand(numCards: Int): List<Card> {
    val colors = listOf("red","green","blue","white","yellow")

    val hand = mutableListOf<Card>()
    for (i in 0..numCards) {
        hand.add( Card( colors[Random.nextInt(colors.size)], Random.nextInt(4)+1))
    }
    return hand
}