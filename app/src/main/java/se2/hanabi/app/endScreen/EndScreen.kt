package se2.hanabi.app.endScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.endScreen.endAnimations.BombLauncher
import se2.hanabi.app.endScreen.endAnimations.FireworkLauncher
import se2.hanabi.app.gamePlayUI.GamePlayViewModel

@Composable
fun EndScreen(onBackToMenu: () -> Unit) {
    val viewModel : GamePlayViewModel = viewModel()

    val gameLost = viewModel.gameLost.collectAsState().value
    val finalScore = viewModel.currentScore.collectAsState().value

    val winMessage = "\uD83C\uDF89 Congratulations! You win! \uD83C\uDF89"
    val loseMessage = "\uD83D\uDE2D Game Over! You lose! \uD83D\uDE2D"

    val alpha = remember { Animatable(0f) }
    val fadeInDelay = 2000
    val fadeInOver = remember { mutableStateOf<Boolean>(false) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = fadeInDelay, easing = LinearOutSlowInEasing),
        )
        fadeInOver.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha.value)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF282828).copy(alpha = 0.5f), Color(0xFF000000).copy(alpha = 0.85f))
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { fadeInOver.value = true},
            )

    ) {
        if (fadeInOver.value) {
            val alpha = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200),
                )
                fadeInOver.value = true
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .alpha(alpha.value),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (gameLost) loseMessage else winMessage,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Final score: $finalScore/${viewModel.getMaxScore()}",
                    fontSize = 20.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = onBackToMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp).height(50.dp)
                ) {
                    Text(
                        text = "Back to Main Menu",
                        color = Color(0xFF28C76F),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


            }
            if (gameLost) {
                BombLauncher() { }
            } else {
                FireworkLauncher() { }
            }
        }
    }
}