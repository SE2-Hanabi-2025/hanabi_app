package se2.hanabi.app

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import se2.hanabi.app.model.Card
import se2.hanabi.app.gamePlayUI.getCardImageName

class CardUITest {

    private fun assertCardImageName(color: Card.Color, number: Int, expected: String) {
        val card = Card(color = color, value = number)
        val imageName = getCardImageName(card)
        assertEquals(expected, imageName)
    }

    @Test
    fun testGetCardImageName() {
        assertCardImageName(Card.Color.RED, 5, "red_5")
        assertCardImageName(Card.Color.BLUE, 3, "blue_3")
        assertCardImageName(Card.Color.GREEN, 7, "green_7")
    }
}

