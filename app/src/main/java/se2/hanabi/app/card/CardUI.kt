package se2.hanabi.app.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import se2.hanabi.app.R

const val aspectRatio = 1.51f
val cardWidth = 66.dp
val cardHeight = cardWidth.times(aspectRatio)

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
fun CardItem(
    modifier: Modifier = Modifier,
    card: Card,
    flipCardState: Boolean,
    isPortrait: Boolean = true,
    rotationAmountZ: Float = 0f
) {
    var isFlipped by remember { mutableStateOf(flipCardState) } // track the current side of the card
    val rotationAmountY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        label = "cardFlip"  // for debugging
    ) // animation progress, where 0f (front), 180f (back)

    Box(
        modifier = modifier
            .size( if (isPortrait) DpSize(cardWidth,cardHeight) else DpSize(cardHeight, cardWidth))
            .clickable { isFlipped = !isFlipped } //toggle when clicked
            .graphicsLayer {
                rotationY = rotationAmountY
                rotationZ = rotationAmountZ
                cameraDistance = 12f * density
            }
    ) {
        if (rotationAmountY <= 90f) {
            //FRONT side
            val imageName = getCardImageName(card)
            val imageID = androidx.compose.ui.platform.LocalContext.current.resources.getIdentifier(
                imageName, "drawable", androidx.compose.ui.platform.LocalContext.current.packageName
            )
            Image(
                modifier =
                    Modifier
                        .requiredSize(cardWidth, cardHeight)
                        .graphicsLayer {
                            rotationZ = if (isPortrait) 0f else 90f
                        }
                        .offset(x = 0.dp, y = if (!isPortrait) (-(cardHeight-cardWidth)/2) else 0.dp),
                painter = painterResource(id = imageID),
                contentDescription = "Front side: $imageName image",
                contentScale = ContentScale.Fit
            )
        } else {
            //BACK side
            Image(
                modifier =
                    Modifier
                        .requiredSize(cardWidth, cardHeight)
                        .graphicsLayer {
                            rotationZ = if (isPortrait) 0f else 90f
                        }
                        .offset(x = 0.dp, y = if (!isPortrait) (-(cardHeight-cardWidth)/2) else 0.dp),
                painter = painterResource(id = R.drawable.card_backside),
                contentDescription = "Card back",
                contentScale = ContentScale.Fit
            )
        }
    }
}