package se2.hanabi.app.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import se2.hanabi.app.gamePlayUI.BackGlow

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
 * @param isFlipped True -> "Backside", False -> "Frontside"
 */
@Composable
fun CardItem(
    modifier: Modifier = Modifier,
    card: Card,
    isFlipped: Boolean,
    isPortrait: Boolean = true,
    rotationAmountZ: Float = 0f,
    isSelected: Boolean = false,
    isHighlighted: Boolean = isSelected,
    onClick: () -> Unit = {},
    highlightColor: Color = Color.White
) {
    val rotationAmountY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        label = "cardFlip"  // for debugging
    ) // animation progress, where 0f (front), 180f (back)

    var actualCardWidth = if (isPortrait) cardWidth else cardHeight
    var actualCardHeight = if (isPortrait) cardHeight else cardWidth



    Box(
        modifier = modifier
            .size(actualCardWidth, actualCardHeight )
            .graphicsLayer {
                rotationY = rotationAmountY
                rotationZ = rotationAmountZ
                cameraDistance = 12f * density
            }
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton,
            ) //toggle when clicked
    ) {
        if (isHighlighted) {
            BackGlow(
                width = actualCardWidth,
                height = actualCardHeight,
                glowSize = 12.dp,
                glowColor = highlightColor,
            )
        }

        val imageName = if(rotationAmountY <= 90f) getCardImageName(card) else "card_backside"
        val imageID = LocalContext.current.resources.getIdentifier(
            imageName, "drawable", LocalContext.current.packageName
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
    }
}