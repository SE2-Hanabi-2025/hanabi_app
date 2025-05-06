package se2.hanabi.app.Model

data class Hint private constructor(
        private val type: HintType,
        private val color: Card.Color? =null,
        private val value: Int? = null
    ) {

    constructor(colorIn: Card.Color?) : this(type=HintType.COLOR, color=colorIn)
    constructor(valueIn: Int) : this(type=HintType.VALUE, value=valueIn)

    //getters
    fun getHintType(): HintType {
        return type
    }

    fun getColor(): Card.Color? {
        return color
    }

    fun getValue(): Int? {
        return value
    }

}