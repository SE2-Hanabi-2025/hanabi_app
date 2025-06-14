package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.model.Card
import kotlin.math.roundToInt

/**
 * PlayersCardsUI displays all the players hands based on a set of hands of cards.
 * this includes:
 * - player's hand at the bottom of the screen with card faces not visible
 * - other players hands on the side of the screen with the card faces visible.
 */
@Composable
fun PlayersCardsUI(
    landscape: Boolean,
    cardSizeDp: DpSize
) {
    val viewModel: GamePlayViewModel = viewModel()
    PlayersHand(
        cardSizeDp = cardSizeDp,
        hand = viewModel.thisPlayersHand.collectAsState().value,
        onCardClick = viewModel::onPlayersCardClick,
        selectedCard = viewModel.selectedCardId.collectAsState().value
    )
    OtherPlayersHands(
        cardSizeDp = cardSizeDp,
        hands = viewModel.otherPlayersHands.collectAsState().value,
        onOtherPlayersHandClick = viewModel::onOtherPlayersHandClick,
        selectedHandIndex = viewModel.selectedPlayerId.collectAsState().value,
//        thisPlayerIndex = viewModel.thisPlayerId.collectAsState().value
    )
    if (viewModel.selectedPlayerId.collectAsState().value != -1) {
        HintSelector(
            landscape = landscape,
            cardSizeDp = cardSizeDp,
            selectedHint = viewModel.selectedHint.collectAsState().value,
            onHintClick = viewModel::onHintClick,
        )
    }
}

@Composable
fun PlayersHand(
    cardSizeDp: DpSize,
    hand: List<Int>,
    onCardClick: (Int) -> Unit,
    selectedCard: Int?
) {
    val viewModel: GamePlayViewModel = viewModel()
    val playerId by viewModel.thisPlayer.collectAsState()
    val players by viewModel.players.collectAsState()
    
    // Find current player's name
    val playerName = players.find { it.id == playerId }?.name ?: "Spieler $playerId"
    
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Display player's name and ID
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 130.dp)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            androidx.compose.material3.Text(
                text = playerName,
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
            androidx.compose.material3.Text(
                text = "ID: $playerId",
                color = Color.White.copy(alpha = 0.7f),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp
                )
            )
        }
        
        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            Arrangement.SpaceEvenly,
        ) {
            hand.forEach() { cardId ->
                CardItem(
                    cardSizeDp = cardSizeDp,
                    card = Card(color=Card.Color.RED, value=1, id = -1), // dummy card: red|1 id = -1
                    isFlipped = true,
                    isSelected = cardId == selectedCard,
                    onClick = { onCardClick(cardId) },
                    colorHint = viewModel.cardsShowingColorHints.collectAsState().value[cardId],
                    valueHint = viewModel.cardsShowingValueHints.collectAsState().value[cardId],
                )
            }
        }
    }
}

@Composable
fun OtherPlayersHands(
    cardSizeDp: DpSize,
    hands: Map<Int, List<Card>>,
    onOtherPlayersHandClick: (Int) -> Unit,
    selectedHandIndex: Int,
){
    val viewModel: GamePlayViewModel = viewModel()
    val players by viewModel.players.collectAsState()
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Create a map of player IDs to player names
    val playerMap = players.associateBy { it.id }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { newSize -> boxSize = newSize }
    ) {

        var rotationAmountZ = remember { mutableFloatStateOf(0f) }
        var handOffsetX by remember { mutableStateOf(0f) }
        var handOffsetY by remember { mutableStateOf(0f) }

        hands.entries.forEachIndexed() { index, hand ->
            if (index % 2 == 0) {// right hand side of screen
                rotationAmountZ.floatValue = -90f
                handOffsetX = boxSize.width.toFloat()
            } else {
                rotationAmountZ.floatValue = 90f
                handOffsetX = 0f
            }

            handOffsetY =
                if (index > 1) {// offset top two hands to be at third of the screen down form top
                    boxSize.height * 0.25f
                } else {
                    boxSize.height * 0.6f
                }
            val handOffset = Offset(handOffsetX, handOffsetY)
              // Find the player details
            val playerId = hand.key
            val playerName = playerMap[playerId]?.name ?: "Spieler $playerId"
            
            OtherPlayersHand(
                cardSizeDp = cardSizeDp,
                offset = handOffset,
                hand = hand,
                playerId = playerId,
                playerName = playerName,
                rotationAmountZ = rotationAmountZ.floatValue,
                isSelected = playerId == selectedHandIndex,
                onClick = { onOtherPlayersHandClick(playerId) }
            )
        }
    }
}

@Composable
fun OtherPlayersHand(
    cardSizeDp: DpSize,
    offset: Offset,
    hand: Map.Entry<Int, List<Card>>,
    playerId: Int,
    playerName: String,
    rotationAmountZ: Float,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val viewModel: GamePlayViewModel = viewModel()
    var rowSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .offset { IntOffset((offset.x-rowSize.width/2).roundToInt(), offset.y.roundToInt()) }
            .graphicsLayer {
                rotationZ = rotationAmountZ
            }
            .onSizeChanged { newSize -> rowSize = newSize },
        contentAlignment = Alignment.Center
        ) {
        // Player name and ID display
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = (-120).dp)
                .graphicsLayer {
                    rotationZ = -rotationAmountZ // Counter-rotate so text is always upright
                }
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            androidx.compose.material3.Text(
                text = playerName,
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
            androidx.compose.material3.Text(
                text = "ID: $playerId",
                color = Color.White.copy(alpha = 0.7f),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp
                )
            )
        }
        
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
            hand.value.forEachIndexed() { index, card ->
                CardItem(
                    cardSizeDp = cardSizeDp,
                    card = card,
                    isFlipped = false,
                    rotationAmountZ = -30f + index * (60 / hand.value.size), //60 degree arc
                    onClick = onClick,
                    isHighlighted = isSelected && (
                            card.color == viewModel.selectedHint.collectAsState().value?.getColor() ||
                                    card.value == viewModel.selectedHint.collectAsState().value?.getValue()
                            ),
                    highlightColor = if (viewModel.selectedHint.collectAsState().value?.getColor()!=null) colorFromColorEnum(card.color) else Color.White,
                    colorHint = viewModel.cardsShowingColorHints.collectAsState().value[card.getID()],
                    valueHint = viewModel.cardsShowingValueHints.collectAsState().value[card.getID()],
                )
            }
        }
    }
}