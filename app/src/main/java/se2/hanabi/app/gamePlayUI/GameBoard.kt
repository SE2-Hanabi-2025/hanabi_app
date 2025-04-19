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

/*
displays common game board elements such as: fuse/hint tokens, discard/draw pile, color stacks
 */
@Composable
fun GameBoardUI(
    stackValues: IntArray,
    numRemainingCards: Int,
    lastDiscardedCard: Card
    ) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF566290).copy(alpha = 0.5f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column( //left colomn
            modifier = Modifier
//                .background(Color.Green.copy(alpha = 0.5f)) // for testing
                .padding(boardElementPadding),
            verticalArrangement = Arrangement.spacedBy(boardElementPadding)

        ) {
            FuseTokens(modifier = Modifier) // .weight(1f)
            Column(
//                modifier = Modifier
//                    .background(Color.White.copy(alpha = 0.5f)), // for testing
                verticalArrangement = Arrangement.spacedBy(cardSpacing)

            ) {
                RemainingCardsStack(numRemainingCards = numRemainingCards)
                DiscardedCardsStack(lastDiscardedCard = lastDiscardedCard)
            }
            HintTokens()
        }
        // right column
        ColorStacks(stackValues = stackValues)
    }
}

val cardSpacing = 5.dp
val boardElementPadding = 10.dp
val tokenAreaHeight = ( cardWidth.times(3) + cardSpacing.times(3) - boardElementPadding.times(2) ).div(2)

@Composable
fun HintTokens() {
    Box(modifier = Modifier
        .size(cardHeight, tokenAreaHeight)
//        .background(Color.Red.copy(alpha = 0.5f)) // for testing
        ) {
        //TODO show Tokens
    }
}

@Composable
fun FuseTokens(modifier: Modifier) {
    Box(modifier = modifier
        .size(cardHeight, tokenAreaHeight)
//        .background(Color.Red.copy(alpha = 0.5f)) // for testing
    ) {
        //TODO show Tokens
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
//            .background(Color.White.copy(alpha = 0.5f)) // for testing
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

