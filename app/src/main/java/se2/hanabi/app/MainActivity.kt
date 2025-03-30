package se2.hanabi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se2.hanabi.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StartMenuScreen()
                }
            }
        }
    }
}

@Composable
fun StartMenuScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_image), // Replace with your image
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // or ContentScale.FillBounds for different scaling
        )
        Text(
            text = "Hanabi!",
            fontFamily = FontFamily.Cursive,
            color = Color(0xFFfbf7b5),
            fontSize = 100.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 1f),
                    offset = Offset(-0f, 0f),
                    blurRadius = 50f
                )
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2ecc71),
                    contentColor = Color.White
                ),
                border = BorderStroke(5.dp, Color.White),
                modifier = Modifier
                    .padding(16.dp)
                    .width(200.dp)
                    .height(60.dp)
            ) {
                Text(
                    text = "Connect To Lobby",
                    textAlign = TextAlign.Center
                )
            }
            Button(onClick = { /*TODO*/ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White
                ),
                border = BorderStroke(5.dp, Color.White),
                modifier = Modifier
                    .padding(1.dp)
                    .width(200.dp)
                    .height(60.dp)
            ) {
                Text("Create Lobby")
            }
        }
        IconButton(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .padding(16.dp)
                .size(60.dp)
                .align(Alignment.BottomStart)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.Black,
                modifier = Modifier.size(100.dp)  // Große Icon-Größe
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartMenuScreenPreview() {
    AppTheme {
        StartMenuScreen()
    }
}
