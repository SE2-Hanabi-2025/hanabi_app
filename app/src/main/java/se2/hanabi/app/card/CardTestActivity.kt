package se2.hanabi.app.card

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class CardTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CardTestScreen()
        }
    }

    @Preview
    @Composable
    fun CardTestScreen() {
        // Example cards to display
        val cards = listOf(
            Card(color = "Red", number = 5),
            Card(color = "Blue", number = 3),
            Card(color = "Green", number = 4),
            Card(color = "Yellow", number = 2),
            Card(color = "White", number = 1)
        )

        Column(modifier = Modifier.padding(30.dp)) {
            cards.forEach { card ->
                CardItem(
                    card = card,
                    isFlipped = false  // frontside by default
                )
            }
        }
    }

}
