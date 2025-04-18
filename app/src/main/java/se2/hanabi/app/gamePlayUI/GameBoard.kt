package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import se2.hanabi.app.card.Card
import se2.hanabi.app.card.CardItem

/*
displays common game board elements such as: fuse/hint tokens, discard/draw pile, color stacks
 */
@Composable
fun GameBoardUI(stackValues: IntArray) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column {
            //FuseTokens()
            //DrawStacks()
            //DiscardStacks()
            //HintTokens()
        }
        ColorStacks(stackValues = stackValues)
    }
}

@Composable
fun EmptyStack(
    modifier: Modifier = Modifier,
    isPortrait: Boolean = true,
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
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(10.dp)
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

