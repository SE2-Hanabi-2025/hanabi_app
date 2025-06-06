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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    val winMessage = "Congratulations!"
    val loseMessage = "Game Over!"

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
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gameLost) Color.Red else Color(0xFFF2FF90),
                    fontFamily = FontFamily.Cursive,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Final score: $finalScore/${viewModel.getMaxScore()}",
                    fontFamily = FontFamily.Cursive,
                    fontSize = 30.sp,
                    color = Color(0xFFF2FF90),
                )

                Spacer(modifier = Modifier.height(48.dp))
                Box(
                    modifier = Modifier

                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF282828).copy(alpha = 0.35f), Color(0xFF282828).copy(alpha = 0.9f))
                            )
                        )
                        .clickable( onClick = onBackToMenu ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 15.dp),
                        text = "Back to main menu",
                        color = Color(0xFFF2FF90),
                        fontFamily = FontFamily.Cursive,
                        fontSize = 25.sp,
                    )
                }


            }
        }
        if (gameLost) {
            BombLauncher() { }
        } else {
            FireworkLauncher() { }
        }
    }
}