package se2.hanabi.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import se2.hanabi.app.screens.StartMenuActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startActivity(Intent(this, StartMenuActivity::class.java))
        finish() // Optional: Schließt MainActivity, damit der Benutzer nicht zurückkehren kann
    }
}
