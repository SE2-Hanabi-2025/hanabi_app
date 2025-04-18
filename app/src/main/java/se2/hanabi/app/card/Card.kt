package se2.hanabi.app.card

/**
 * Represents a single Hanabi Card
 */

data class Card (val color: String,   // "white"/"yellow"/"red"/"green"/"blue"
                 val number: Int)     // 1..5