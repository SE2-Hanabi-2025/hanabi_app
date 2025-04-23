package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import se2.hanabi.app.card.Card
import se2.hanabi.app.card.CardItem
import kotlin.math.roundToInt

/**
 * PlayersCardsUI displays all the players hands based on a set of hands of cards.
 * this includes:
 * - player's hand at the bottom of the screen with card faces not visible
 * - other players hands on the side of the screen with the card faces visible.
 */
@Composable
fun PlayersCardsUI(hands: List<List<Card>>) {
    // assumes player's hand will be index 0.
    // will probobaly need editing to integrate with game logic.
    PlayersHand(hands[0])
    OtherPlayersHands(hands.slice(1..hands.lastIndex))
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
            var selectedCardIndex by remember { mutableStateOf(-1) }
            hand.forEachIndexed() {index, card ->
                CardItem(
                    card = card,
                    isFlipped = true,
                    isSelected = index == selectedCardIndex,
                    onClick = {
                        selectedCardIndex = if (index == selectedCardIndex) -1 else index
                    }
                )
            }
        }
    }
}

@Composable
fun OtherPlayersHands(hands: List<List<Card>>) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedHandIndex by remember { mutableStateOf(-1) }

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
            OtherPlayersHand(
                offset = handOffset,
                hand = hand,
                rotationAmountZ = rotationAmountZ.floatValue,
                isSelected = index == selectedHandIndex,
                onClick = {selectedHandIndex = if (index == selectedHandIndex) -1 else index}
            )
            if (selectedHandIndex!=-1) {
                HintSelecter(
                    modifier = Modifier.align(alignment = Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun OtherPlayersHand(
    offset: Offset,
    hand: List<Card>,
    rotationAmountZ: Float,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    var rowSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .offset { IntOffset((offset.x-rowSize.width/2).roundToInt(), offset.y.roundToInt()) } //-rowSize.width/2).dp
            .graphicsLayer {
                rotationZ = rotationAmountZ
            }
            .onSizeChanged { newSize -> rowSize = newSize },
        contentAlignment = Alignment.Center

        ) {
        if (isSelected) {
            BackGlow(
                width = with (LocalDensity.current) {rowSize.width.toDp() - 20.dp},
                height = with (LocalDensity.current) {rowSize.height.toDp() - 20.dp },
                glowSize = 30.dp,
                )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy((-45.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            hand.forEachIndexed() { index, card ->
                CardItem(
                    card = card,
                    isFlipped = false,
                    rotationAmountZ = -30f + index * (60 / hand.size), //60 degree arc
                    onClick = onClick
                )
            }
        }
    }
}