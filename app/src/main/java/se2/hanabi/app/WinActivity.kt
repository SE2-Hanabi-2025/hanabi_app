package se2.hanabi.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se2.hanabi.app.MainActivity
import androidx.compose.material3.*
import se2.hanabi.app.EndAnimations.FireworkLauncher

class WinActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WinScreen(
                onBackToMenu = {
                    //Navigate back to MainActiviy and clear the back stack
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}
@Composable
fun WinScreen(onBackToMenu: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF282828), Color(0xFF000000))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\uD83C\uDF89 Congratulations! You Win! \uD83C\uDF89",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Congratulations on completing the stack!",
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onBackToMenu,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp).height(50.dp)
            ) {
                Text(
                    text = "Bact to Main Menu",
                    color = Color(0xFF28C76F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }


        }
        FireworkLauncher() {  }
    }
}
