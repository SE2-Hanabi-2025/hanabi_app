package se2.hanabi.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    internal val name: String,
    private val id: Int
)