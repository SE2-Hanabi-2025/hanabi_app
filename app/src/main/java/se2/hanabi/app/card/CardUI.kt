package se2.hanabi.app.card


// Returns name of the corresponding card image, following the schema "color_number"
fun getCardImageName(card: Card): String {
    return "${card.color.lowercase()}_${card.number}"
}
