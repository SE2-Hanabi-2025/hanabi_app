package se2.hanabi.app.gamePlayUI

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

enum class TokenType {
    hint,
    fuse,
}

val fuseTokenSize = 50.dp
val hintTokenSize = 30.dp

@Composable
fun Token(
    modifier: Modifier = Modifier,
    type: TokenType = TokenType.hint,
    isFlippedState: Boolean
) {
    var isFlipped by remember { mutableStateOf(isFlippedState) }
    val rotationAmountY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        label = "tokenFlip"
    )

    val fileName = type.toString() + "_" + if (rotationAmountY >90f) "back" else "front"
    val imageID = LocalContext.current.resources.getIdentifier(
        fileName,
        "drawable",
        LocalContext.current.packageName
    )
    Image(
        modifier = Modifier
            .size(if (type==TokenType.hint) hintTokenSize else fuseTokenSize)
            .clickable { isFlipped = !isFlipped }
            .graphicsLayer {
                rotationY = rotationAmountY
                cameraDistance = 12f * density
            },
        painter = painterResource(id = imageID),
        contentDescription = fileName,
        contentScale = ContentScale.Fit
    )
}



