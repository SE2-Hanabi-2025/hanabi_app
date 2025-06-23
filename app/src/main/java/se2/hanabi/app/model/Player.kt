package se2.hanabi.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val name: String,
    val id: Int,
    val avatarResID: Int
)