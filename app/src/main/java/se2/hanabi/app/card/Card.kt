package se2.hanabi.app.card

/**
 * Represents a single Hanabi Card
 */

data class Card(
    val color: String,  // "white"/"yellow"/"red"/"green"/"blue"
    val number: Int, // 1..5
    private val id: Int = nextID++
) {
    companion object {
        private var nextID = 0
    }

    fun getID():Int {
        return id
    }
}