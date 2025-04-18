package se2.hanabi.app.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import se2.hanabi.app.R

// Returns name of the corresponding card image, following the schema "color_number"
fun getCardImageName(card: Card): String {
    return "${card.color.lowercase()}_${card.number}"
}

/**
 * UI-function to display a single card.
 * Features:
 *          - default side: back
 *          - flip the card by an external trigger or tap
 *
 * @param card The Card model with color and number.
 * @param modifier Composable object, default
 * @param flipCardState True -> "Backside", False -> "Frontside"
 */
@Composable
fun CardItem(card: Card, modifier: Modifier = Modifier, flipCardState: Boolean) {

    var isFlipped by remember { mutableStateOf(flipCardState) } // track the current side of the card
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        label = "cardFlip"  // for debugging
    ) // animation progress, where 0f (front), 180f (back)

    Box(
        modifier = modifier
            .size(100.dp)
            .clickable { isFlipped = !isFlipped } //toggle when clicked
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            //FRONT side
            val imageName = getCardImageName(card)
            val imageID = androidx.compose.ui.platform.LocalContext.current.resources.getIdentifier(
                imageName, "drawable", androidx.compose.ui.platform.LocalContext.current.packageName
            )
            Image(
                painter = painterResource(id = imageID),
                contentDescription = "Front side: $imageName image",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            //BACK side
            Image(
                painter = painterResource(id = R.drawable.card_backside),
                contentDescription = "Card back",
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        rotationY = 180f //flip to backside
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}