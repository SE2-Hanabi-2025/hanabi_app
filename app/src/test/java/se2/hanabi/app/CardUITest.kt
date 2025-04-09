package se2.hanabi.app

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import se2.hanabi.app.card.Card
import se2.hanabi.app.card.getCardImageName

class CardUITest {

    private fun assertCardImageName(color: String, number: Int, expected: String) {
        val card = Card(color = color, number = number)
        val imageName = getCardImageName(card)
        assertEquals(expected, imageName)
    }

    @Test
    fun testGetCardImageName() {
        assertCardImageName("Red", 5, "red_5")
        assertCardImageName("BLUE", 3, "blue_3")
        assertCardImageName("green", 7, "green_7")
    }
}

