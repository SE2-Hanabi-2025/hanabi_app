import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow

class WebSocketService {
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }
    private val url = "ws://10.0.2.2:8080/ws"

    val incomingMessages = MutableSharedFlow<String>()

    fun connect() {
        CoroutineScope(Dispatchers.IO).launch {
            client.webSocket(urlString = url) {
                launch {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            incomingMessages.emit(frame.readText())
                        }
                    }
                }
                send("Hello from Android!")
            }
        }
    }

    suspend fun sendMessage(message: String) {
        client.webSocket(urlString = url) {
            send(message)
        }
    }
}