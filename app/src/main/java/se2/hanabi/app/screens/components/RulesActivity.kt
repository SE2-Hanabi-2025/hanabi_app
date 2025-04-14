package se2.hanabi.app.screens.components

import HanabiRulesScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import se2.hanabi.app.ui.theme.ClientTheme

class RulesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClientTheme {
                HanabiRulesScreen()
            }
        }
    }
}