package se2.hanabi.app.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

// Returns name of the corresponding card image, following the schema "color_number"
fun getCardImageName(card: Card): String {
    return "${card.color.lowercase()}_${card.number}"
}

/**
 * Displays a card image.
 * @param card The Card model with color and number.
 */
@Composable
fun CardItem(card: Card) {
    Box(
        modifier = Modifier
            .size(100.dp)
    ) {
        val imageName = getCardImageName(card)
        val resId = androidx.compose.ui.platform.LocalContext.current.resources.getIdentifier(
                imageName, "drawable", androidx.compose.ui.platform.LocalContext.current.packageName)

        Image(
            painter = painterResource(id = resId),
            contentDescription = "$imageName image",
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Fit
        )
    }
}