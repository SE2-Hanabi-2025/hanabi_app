package se2.hanabi.app.utils


object ServerAddressManager {
   
    enum class ServerEnvironment {
        EMULATOR,      // Android Emulator (10.0.2.2)
        LOCAL_NETWORK, // Lokales Netzwerk (z.B. 192.168.x.x)
        UNI_SERVER     // Uni-Server
    }


 //einfach hier ip-Adresse und Port ändern, um die Verbindung zu einem anderen Server herzustellen
    private val CURRENT_ENVIRONMENT = ServerEnvironment.EMULATOR
   
    private const val EMULATOR_HOST = "10.0.2.2"
    private const val LOCAL_NETWORK_HOST = "192.168.0.77" 
    private const val UNI_SERVER_HOST = "se2-demo.aau.at"
    
   
    private const val EMULATOR_PORT = 8080
    private const val LOCAL_NETWORK_PORT = 8080
    private const val UNI_SERVER_PORT = 8080
    
    
    private const val EMULATOR_USES_HTTPS = false
    private const val LOCAL_NETWORK_USES_HTTPS = false
    private const val UNI_SERVER_USES_HTTPS = false
    
    
    private object ApiPaths {
        const val STATUS = "/status"
        const val CREATE_LOBBY = "/create-lobby"
        const val JOIN_LOBBY = "/join-lobby"
        const val START_GAME = "/start-game"
        const val LEAVE_LOBBY = "/leave-lobby"
        const val LOBBY = "/lobby"
        const val WEBSOCKET_GAME = "/ws/game"
        const val API_GAME = "/api/game"
    }
    
    
    private fun getCurrentEnvironment(): ServerEnvironment {
        return CURRENT_ENVIRONMENT
    }
     
    private fun getHost(): String {
        val env = getCurrentEnvironment()
        return when (env) {
            ServerEnvironment.LOCAL_NETWORK -> LOCAL_NETWORK_HOST
            ServerEnvironment.UNI_SERVER -> UNI_SERVER_HOST
            ServerEnvironment.EMULATOR -> EMULATOR_HOST
        }
    }
    


    private fun getPort(): Int {
        val env = getCurrentEnvironment()
        return when (env) {
            ServerEnvironment.LOCAL_NETWORK -> LOCAL_NETWORK_PORT
            ServerEnvironment.UNI_SERVER -> UNI_SERVER_PORT
            ServerEnvironment.EMULATOR -> EMULATOR_PORT
        }
    }


    private fun usesHttps(): Boolean {
        return when (getCurrentEnvironment()) {
            ServerEnvironment.LOCAL_NETWORK -> LOCAL_NETWORK_USES_HTTPS
            ServerEnvironment.UNI_SERVER -> UNI_SERVER_USES_HTTPS
            ServerEnvironment.EMULATOR -> EMULATOR_USES_HTTPS
        }
    }
    


    private fun getHttpProtocol(): String {
        return if (usesHttps()) "https://" else "http://"
    }
    


    private fun getWsProtocol(): String {
        return if (usesHttps()) "wss://" else "ws://"
    }
    


    private fun getHostWithPort(): String {
        val host = getHost()
        val port = getPort()
        // Standard-Ports müssen nicht angegeben werden
        return if ((usesHttps() && port == 443) || (!usesHttps() && port == 80)) {
            host
        } else {
            "$host:$port"
        }
    }
    


    val BASE_HTTP_URL: String
        get() = "${getHttpProtocol()}${getHostWithPort()}"
    


    val BASE_WEBSOCKET_URL: String
        get() = "${getWsProtocol()}${getHostWithPort()}"
    

    val GAME_API_URL: String
        get() = "$BASE_HTTP_URL${ApiPaths.API_GAME}"
    

    val STATUS_URL: String
        get() = "$BASE_HTTP_URL${ApiPaths.STATUS}"
    

    val GAME_WEBSOCKET_URL: String
        get() = "$BASE_WEBSOCKET_URL${ApiPaths.WEBSOCKET_GAME}"
    


    fun getGameUrl(lobbyId: String): String = "$GAME_API_URL/$lobbyId/status"
    


    fun getLobbyPlayersUrl(lobbyCode: String): String = "$BASE_HTTP_URL${ApiPaths.LOBBY}/$lobbyCode/players"
    


    fun getStartGameUrl(lobbyCode: String): String = "$BASE_HTTP_URL${ApiPaths.START_GAME}/$lobbyCode"
    


    fun getJoinLobbyUrl(lobbyCode: String): String = "$BASE_HTTP_URL${ApiPaths.JOIN_LOBBY}/$lobbyCode"
    


    fun getLeaveLobbyUrl(lobbyCode: String, playerId: Int): String = "$BASE_HTTP_URL${ApiPaths.LEAVE_LOBBY}/$lobbyCode/$playerId"
    

     //Erstellt eine WebSocket-URL für ein Spiel

    fun getGameWebSocketUrl(lobbyId: String, playerId: Int): String = "$GAME_WEBSOCKET_URL?lobbyId=$lobbyId&playerId=$playerId"
    

     //Erstellt eine URL für das Erstellen einer Lobby

    fun getCreateLobbyUrl(): String = "$BASE_HTTP_URL${ApiPaths.CREATE_LOBBY}"
    
    //nur für Debugging
    fun getServerConfigInfo(): Map<String, String> {
        return mapOf(
            "environment" to getCurrentEnvironment().toString(),
            "host" to getHost(),
            "port" to getPort().toString(),
            "usesHttps" to usesHttps().toString(),
            "baseHttpUrl" to BASE_HTTP_URL,
            "baseWebSocketUrl" to BASE_WEBSOCKET_URL
        )
    }
}
