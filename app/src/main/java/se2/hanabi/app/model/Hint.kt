package se2.hanabi.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Hint private constructor(
    private val color: Card.Color? =null,
    private val value: Int? = null
    ) {

    constructor(colorIn: Card.Color?) : this(color=colorIn)
    constructor(valueIn: Int) : this(value=valueIn)

    fun getColor(): Card.Color? {
        return color
    }

    fun getValue(): Int? {
        return value
    }

}