package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun PlayersCardsUI() {
    val viewModel: GamePlayViewModel = viewModel()
    PlayersHand(
        hand = viewModel.hands.collectAsState().value[viewModel.thisPlayerIndex.collectAsState().value],
        onCardClick = viewModel::onPlayersCardClick,
        selectedCard = viewModel.selectedCard.collectAsState().value
    )
    OtherPlayersHands(
        viewModel.hands.collectAsState().value,
        onOtherPlayersHandClick = viewModel::onOtherPlayersHandClick,
        selectedHandIndex = viewModel.selectedHandIndex.collectAsState().value,
        thisPlayerIndex = viewModel.thisPlayerIndex.collectAsState().value
    )
    if (viewModel.selectedHandIndex.collectAsState().value != -1) {
        HintSelector(
            selectedHint = viewModel.selectedHint.collectAsState().value,
            onHintClick = viewModel::onHintClick,
        )
    }
}

@Composable
fun PlayersHand(
    hand: List<Card>,
    onCardClick: (Card?) -> Unit,
    selectedCard: Card?
) {
    val viewModel: GamePlayViewModel = viewModel()
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
            hand.forEach() { card ->
                CardItem(
                    card = card,
                    isFlipped = true,
                    isSelected = card == selectedCard,
                    onClick = { onCardClick(card) },
                    showColorHint = viewModel.shownColorHints.contains(card.getID()),
                    showValueHint = viewModel.shownValueHints.contains(card.getID())
                )
            }
        }
    }
}

@Composable
fun OtherPlayersHands(
    hands: List<List<Card>>,
    onOtherPlayersHandClick: (Int) -> Unit,
    selectedHandIndex: Int,
    thisPlayerIndex: Int,
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { newSize -> boxSize = newSize }
    ) {

        var rotationAmountZ = remember { mutableFloatStateOf(0f) }
        var handOffsetX by remember { mutableStateOf(0f) }
        var handOffsetY by remember { mutableStateOf(0f) }

        var positionIndex = 0
        hands.forEachIndexed() {index, hand ->
            if (index != thisPlayerIndex) {
                if (positionIndex % 2 == 0) {// right hand side of screen
                    rotationAmountZ.floatValue = -90f
                    handOffsetX = boxSize.width.toFloat()
                } else {
                    rotationAmountZ.floatValue = 90f
                    handOffsetX = 0f
                }

                handOffsetY =
                    if (positionIndex > 1) {// offset top two hands to be at third of the screen down form top
                        boxSize.height * 0.25f
                    } else {
                        boxSize.height * 0.6f
                    }
                val handOffset = Offset(handOffsetX, handOffsetY)
                OtherPlayersHand(
                    offset = handOffset,
                    hand = hand,
                    rotationAmountZ = rotationAmountZ.floatValue,
                    isSelected = index == selectedHandIndex,
                    onClick = { onOtherPlayersHandClick(index) }
                )
                positionIndex++
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
    val viewModel: GamePlayViewModel = viewModel()
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
                    onClick = onClick,
                    isHighlighted = isSelected && (
                            card.color == viewModel.selectedHint.collectAsState().value ||
                                    card.number.toString() == viewModel.selectedHint.collectAsState().value
                            ),
                    highlightColor = if (card.color == viewModel.selectedHint.collectAsState().value) colorFromString(card.color) else Color.White,
                    showColorHint = viewModel.shownColorHints.contains(card.getID()),
                    showValueHint = viewModel.shownValueHints.contains(card.getID())
                )
            }
        }
    }
}