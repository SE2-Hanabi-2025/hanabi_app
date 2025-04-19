package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se2.hanabi.app.card.Card
import se2.hanabi.app.card.CardItem
import se2.hanabi.app.card.cardHeight
import se2.hanabi.app.card.cardWidth

/**
 * GameBoardUI display the features of the game board.
 * These include: fuse/hint tokens, discard/draw pile, and color stacks
 *
 *@param stackValues IntArray of the highest value in each stack. In the order of GamePlayUI.colors
 *@param numRemainingCards Int representing the remaining cards that can be drawn.
 *@param lastDiscardedCard Card will be shown on the top of the discarded card pile.
 *@param numRemainingHintTokens Used to display the how many hint tokens are still available to the players.
 *@param numRemainingFuseTokens Used to display the remaining fuse token are left before game over.
 */
@Composable
fun GameBoardUI(
    stackValues: IntArray,
    numRemainingCards: Int,
    lastDiscardedCard: Card,
    numRemainingHintTokens: Int,
    numRemainingFuseTokens: Int,
    ) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF566290).copy(alpha = 0.5f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        //left column
        Column(
            modifier = Modifier
                .padding(boardElementPadding),
            verticalArrangement = Arrangement.spacedBy(boardElementPadding)
        ) {
            FuseTokens(numRemaining = numRemainingFuseTokens)
            Column(
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                RemainingCardsStack(numRemainingCards = numRemainingCards)
                DiscardedCardsStack(lastDiscardedCard = lastDiscardedCard)
            }
            HintTokens(numRemaining = numRemainingHintTokens)
        }
        // right column
        ColorStacks(stackValues = stackValues)
    }
}

val cardSpacing = 5.dp
val boardElementPadding = 10.dp
val tokenAreaHeight = ( cardWidth.times(3) + cardSpacing.times(3) - boardElementPadding.times(2) ).div(2)

@Composable
fun HintTokens(numRemaining: Int) {
    val totalNumTokens = 8
    Column(modifier = Modifier
        .size(cardHeight, tokenAreaHeight),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(){ // tokens 7,8 on top row
            for (tokenIndex in 7..8) {
                Token(
                    type = TokenType.hint,
                    isFlipped = tokenIndex > numRemaining
                )
            }
        }
        Row(){ // tokens 4,5,6 on mid row
            for (tokenIndex in 4..6) {
                Token(
                    type = TokenType.hint,
                    isFlipped = tokenIndex > numRemaining
                )
            }
        }
        Row(){ // tokens 1,2,3 on bottom row
            for (tokenIndex in 1..3) {
                Token(
                    type = TokenType.hint,
                    isFlipped = tokenIndex > numRemaining
                )
            }
        }
    }
}

@Composable
fun FuseTokens(numRemaining: Int) {
    val totalNumTokens = 3
    Column(modifier = Modifier
        .size(cardHeight, tokenAreaHeight),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Token( // fuse token num 3 on top row
            type = TokenType.fuse,
            isFlipped = 3 > numRemaining
        )
        Row() { // fuse token num 1 and 2 on bottom row
            for (tokenIndex in 1..2) {
                Token(
                    type = TokenType.fuse,
                    isFlipped = tokenIndex > numRemaining
                )
            }
        }
    }
}

@Composable
fun RemainingCardsStack(
    modifier: Modifier = Modifier,
    numRemainingCards: Int
) {
    Box(contentAlignment = Alignment.Center ) {
        if (numRemainingCards==0) {
            EmptyStack()
        } else {
            CardItem(
                card = Card("red", 1),
                flipCardState = true,
                isPortrait = false,
            )
        }
        Text(
            modifier = Modifier.alpha(if (numRemainingCards==0) 0.5f else 1f),
            text = "$numRemainingCards",
            fontFamily = FontFamily.Cursive,
            color = Color(0xFFF2FF90),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    blurRadius = 50f
                )
            )
        )
    }
}

@Composable
fun DiscardedCardsStack(
    lastDiscardedCard: Card
) {
    CardItem(
        card = Card(lastDiscardedCard.color,lastDiscardedCard.number),
        flipCardState = false,
        isPortrait = false,
    )
}


@Composable
fun EmptyStack(
    modifier: Modifier = Modifier,
    isPortrait: Boolean = false,
) {
    CardItem(
        modifier = Modifier.alpha(0.3f),
        card = Card("red",1),
        flipCardState = true,
        isPortrait = isPortrait,
    )
}

@Composable
fun ColorStacks(
    modifier: Modifier = Modifier,
    stackValues: IntArray,
) {
    Column(
        modifier = Modifier
            .padding(boardElementPadding),
        verticalArrangement = Arrangement.spacedBy(cardSpacing),
    ) {
        colors.forEachIndexed() {index, color ->
            if (stackValues[index]==0) {
                EmptyStack(isPortrait = false)
            } else {
                CardItem(card = Card(color, stackValues[index]), flipCardState = false, isPortrait = false)
            }
        }
    }
}

