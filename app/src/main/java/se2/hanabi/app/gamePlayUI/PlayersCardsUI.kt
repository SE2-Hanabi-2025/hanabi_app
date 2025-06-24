package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.model.Card
import kotlin.math.pow
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
    cardSizeDpIn: DpSize
) {
    val viewModel: GamePlayViewModel = viewModel()
    val highlightedPlayer = viewModel.highlightedPlayer.collectAsState().value
    val cheatHand = viewModel.cheatHand.collectAsState().value
    val currentPlayer = viewModel.currentPlayer.collectAsState().value
    var showCheat by remember { mutableStateOf(false) }
    var cheatUsedThisRound by remember { mutableStateOf(false) }
    var lastPlayer by remember { mutableStateOf(currentPlayer ?: -1) }

    // Reset cheat usage when the round changes
    if (lastPlayer != currentPlayer) {
        cheatUsedThisRound = false
        lastPlayer = currentPlayer ?: -1
    }

    val showRealCards = showCheat && cheatHand.isNotEmpty()
    // Hide cards after 3 seconds
    LaunchedEffect(showCheat) {
        if (showCheat) {
            kotlinx.coroutines.delay(3000)
            showCheat = false
        }
    }
    // Always use the normal hand for selection, hinting, and logic
    val handForLogic = viewModel.thisPlayersHand.collectAsState().value
    PlayersHand(
        cardSizeDp = cardSizeDpIn,
        hand = if (showRealCards) cheatHand.map { it.getID() } else handForLogic,
        onCardClick = viewModel::onPlayersCardClick,
        selectedCard = viewModel.selectedCardId.collectAsState().value,
        isHighlighted = viewModel.isMyTurn.collectAsState().value,
        showRealCards = showRealCards,
        realCards = cheatHand,
        onCheatActivated = {
            if (!cheatUsedThisRound) {
                showCheat = true
                cheatUsedThisRound = true
                viewModel.onCheatRequested()
            }
        }
    )
    // ensure others players cards are display clockwise in terms of player order
    val visibleHands = viewModel.otherPlayersHands.collectAsState().value
    val handsDisplayOrder: MutableMap<Int, List<Card>> =  mutableMapOf()
    val thisPlayerId = viewModel.thisPlayer.collectAsState().value
    val playerIds = viewModel.players.collectAsState().value.map { player -> player.id }
    val thisPlayerIndex = playerIds.indexOf(thisPlayerId)
    val numPLayers = playerIds.size
    for (i in 1..numPLayers-1) {
        val nextPlayerIndex = modPositive(thisPlayerIndex+((-1f).pow(i)*((i+1)/2)).toInt(), numPLayers) // sequence -1, +1, -2, +2, ...
        val nextPlayersID = playerIds[nextPlayerIndex]
        val nextPlayersHand = visibleHands.get(key = nextPlayersID)
        handsDisplayOrder.put(nextPlayersID, nextPlayersHand!!)
    }

    //limit cards from being too small in landscape
    val MIN_CARD_WIDTH_LS = 60.dp
    var cardSizeDp = cardSizeDpIn
    if (landscape) {
        val cardWidth = max(cardSizeDpIn.width, MIN_CARD_WIDTH_LS)
        val cardHeight = cardWidth.times(CARD_ASPECT_RATIO)
        cardSizeDp = DpSize(cardWidth, cardHeight)
    }

    OtherPlayersHands(
        hands = handsDisplayOrder,
        cardSizeDp = cardSizeDp,
        landscape = landscape,
        onOtherPlayersHandClick = viewModel::onOtherPlayersHandClick,
        selectedHandIndex = viewModel.selectedPlayerId.collectAsState().value,
        highlightedPlayer = highlightedPlayer,
    )

    // Restore HintSelector when a player is selected
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
    selectedCard: Int?,
    isHighlighted: Boolean,
    showRealCards: Boolean = false,
    realCards: List<Card> = emptyList(),
    onCheatActivated: (() -> Unit)? = null
) {
    val viewModel: GamePlayViewModel = viewModel()
    val playerId by viewModel.thisPlayer.collectAsState()
    val players by viewModel.players.collectAsState()
    val playerName = players.find { it.id == playerId }?.name ?: "Spieler $playerId"
    var rowSize by remember { mutableStateOf(IntSize.Zero) }
    // Track offset for each card by cardId
    val cardOffsets = remember { mutableStateMapOf<Int, Offset>() }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Display player's name and ID
        val nameTagModifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = cardSizeDp.height.times(1.1f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        try {
                            kotlinx.coroutines.delay(6000)
                            onCheatActivated?.invoke()
                        } finally {
                            awaitRelease()
                        }
                    }
                )
            }
        NameTag(
            modifier = nameTagModifier,
            playerName =playerName,
            isPlayersTurn = viewModel.currentPlayer.collectAsState().value == playerId
        )

        if (isHighlighted) {
            BackGlow(
                width = with (LocalDensity.current) {rowSize.width.toDp() - 20.dp},
                height = with (LocalDensity.current) {rowSize.height.toDp() - 20.dp },
                glowSize = 30.dp,
            )
        }
        
        Row(
            modifier = Modifier
                .padding(5.dp)
                .onSizeChanged { newSize -> rowSize= newSize },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showRealCards && realCards.isNotEmpty()) {
                val density = LocalDensity.current
                val cardWidthPx = with(density) { cardSizeDp.width.toPx() }
                val cardHeightPx = with(density) { cardSizeDp.height.toPx() }
                realCards.forEachIndexed { idx, card ->
                    val cardId = card.getID()
                    val offset = cardOffsets[cardId] ?: Offset.Zero
                    var cardCoordinates: androidx.compose.ui.layout.LayoutCoordinates? = null
                    CardItem(
                        cardSizeDp = cardSizeDp,
                        card = card,
                        isFlipped = false,
                        isSelected = cardId == selectedCard,
                        onClick = { onCardClick(cardId) },
                        colorHint = viewModel.cardsShowingColorHints.collectAsState().value[cardId],
                        valueHint = viewModel.cardsShowingValueHints.collectAsState().value[cardId],
                        modifier = Modifier
                            .offset {
                                IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
                            }
                            .onGloballyPositioned { coordinates ->
                                cardCoordinates = coordinates
                            }
                            .pointerInput(cardId) {
                                detectDragGestures(
                                    onDragStart = { viewModel.startDraggingCard(cardId) },
                                    onDragEnd = {
                                        viewModel.stopDraggingCard()
                                        cardOffsets[cardId] = Offset.Zero
                                        // Try drop on any zone (color stacks, discard, or strike)
                                        viewModel.tryDropOnAnyZone(cardId)
                                    },
                                    onDragCancel = { viewModel.stopDraggingCard(); cardOffsets[cardId] = Offset.Zero },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        cardOffsets[cardId] = cardOffsets[cardId]?.plus(dragAmount) ?: dragAmount
                                        // Convert pointer position to window coordinates for drop zone hit testing
                                        val windowOffset = cardCoordinates?.localToWindow(change.position) ?: change.position
                                        android.util.Log.d("HanabiPlayersCardsUI", "onDrag: cardId=$cardId, dragAmount=$dragAmount, pointer=${change.position}, windowOffset=$windowOffset, cardOffset=${cardOffsets[cardId]}")
                                        viewModel.updatePointerPosition(windowOffset)
                                    }
                                )
                            })
                }
            } else {
                val density = LocalDensity.current
                val cardWidthPx = with(density) { cardSizeDp.width.toPx() }
                val cardHeightPx = with(density) { cardSizeDp.height.toPx() }
                hand.forEach { cardId ->
                    val offset = cardOffsets[cardId] ?: Offset.Zero
                    var cardCoordinates: androidx.compose.ui.layout.LayoutCoordinates? = null
                    CardItem(
                        cardSizeDp = cardSizeDp,
                        card = Card(color=Card.Color.RED, value=1, id = -1), // dummy card for back
                        isFlipped = true,
                        isSelected = cardId == selectedCard,
                        onClick = { onCardClick(cardId) },
                        colorHint = viewModel.cardsShowingColorHints.collectAsState().value[cardId],
                        valueHint = viewModel.cardsShowingValueHints.collectAsState().value[cardId],
                        modifier = Modifier
                            .offset {
                                IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
                            }
                            .onGloballyPositioned { coordinates ->
                                cardCoordinates = coordinates
                            }
                            .pointerInput(cardId) {
                                detectDragGestures(
                                    onDragStart = { viewModel.startDraggingCard(cardId) },
                                    onDragEnd = {
                                        viewModel.stopDraggingCard()
                                        cardOffsets[cardId] = Offset.Zero
                                        // Try drop on any zone (color stacks, discard, or strike)
                                        viewModel.tryDropOnAnyZone(cardId)
                                    },
                                    onDragCancel = { viewModel.stopDraggingCard(); cardOffsets[cardId] = Offset.Zero },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        cardOffsets[cardId] = cardOffsets[cardId]?.plus(dragAmount) ?: dragAmount
                                        // Convert pointer position to window coordinates for drop zone hit testing
                                        val windowOffset = cardCoordinates?.localToWindow(change.position) ?: change.position
                                        viewModel.updatePointerPosition(windowOffset)
                                    }
                                )
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun OtherPlayersHands(
    cardSizeDp: DpSize,
    hands: Map<Int, List<Card>>,
    landscape: Boolean,
    onOtherPlayersHandClick: (Int) -> Unit,
    selectedHandIndex: Int,
    highlightedPlayer: Int
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
                    boxSize.height * ( if (landscape) 0.25f else 0.35f )
                } else {
                    boxSize.height * ( if (landscape) 0.75f else 0.65f )
                }
            val handOffset = Offset(handOffsetX, handOffsetY)
              // Find the player details
            val playerId = hand.key
            val playerName = playerMap[playerId]?.name ?: "Spieler $playerId"
            
            OtherPlayersHand(
                cardSizeDp = cardSizeDp,
                offset = handOffset,
                hand = hand,
                isPlayersTurn = viewModel.currentPlayer.collectAsState().value == playerId,
                playerName = playerName,
                rotationAmountZ = rotationAmountZ.floatValue,
                isSelected = playerId == selectedHandIndex,
                onClick = { onOtherPlayersHandClick(playerId) },
                isHighlighted = playerId == highlightedPlayer
            )
        }
    }
}

@Composable
fun OtherPlayersHand(
    cardSizeDp: DpSize,
    offset: Offset,
    hand: Map.Entry<Int, List<Card>>,
    isPlayersTurn: Boolean,
    playerName: String,
    rotationAmountZ: Float,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    isHighlighted: Boolean,
) {
    val viewModel: GamePlayViewModel = viewModel()
    var rowSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .offset { IntOffset((offset.x-rowSize.width/2).roundToInt(), (offset.y-rowSize.height/2).roundToInt()) }
            .graphicsLayer {
                rotationZ = rotationAmountZ
            }
            .onSizeChanged { newSize -> rowSize = newSize },
        contentAlignment = Alignment.Center
        ) {
        // Player name and ID displayž
        val nameTagModifier = Modifier
            .offset(y = -cardSizeDp.height.times(0.75f))
        NameTag(
            modifier = nameTagModifier,
            playerName = playerName,
            isPlayersTurn = isPlayersTurn,
        )
        
        if (isSelected || isHighlighted) {
            BackGlow(
                width = with (LocalDensity.current) {rowSize.width.toDp() - 20.dp},
                height = with (LocalDensity.current) {rowSize.height.toDp() - 20.dp },
                glowSize = 30.dp,
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy((-cardSizeDp.width.times(0.75f).value.dp)),
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

@Composable
fun NameTag(
    modifier: Modifier = Modifier,
    playerName: String,
    isPlayersTurn: Boolean,
) {
    Text(
        modifier = modifier
            .background(Color.DarkGray.copy(alpha = 0.20f), RoundedCornerShape(40.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        text = playerName,
        color = if (isPlayersTurn) Color.Green else Color.White,
        style = androidx.compose.ui.text.TextStyle(
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    )
}

fun modPositive(x: Int, y: Int): Int {
    return ((x%y) + y ) % y
}