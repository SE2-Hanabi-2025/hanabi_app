package se2.hanabi.app.logic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class GameManagerTest {
    private lateinit var gameManager: GameManager

    @BeforeEach
    fun setup() {
        gameManager = GameManager()
    }

    @Test
    fun drawCard_addsCardAndReducesDeckSize() {
        val initialSize = gameManager.deckSize
        val result = gameManager.drawCard("3")
        assertTrue(result)
        assertEquals(initialSize - 1, gameManager.deckSize)
        assertTrue(gameManager.drawnCards.contains("3"))
    }

    @Test
    fun drawCard_returnFalseIfDeckIsEmpty() {
        repeat (50) { gameManager.drawCard("1")}
        val result = gameManager.drawCard("1")
        assertFalse(result)
    }

    @Test
    fun selectCard_togglesCardSelection() {
        gameManager.drawCard("3")
        gameManager.selectCard(0)
        assertEquals(0, gameManager.selectedCardIndex)
        gameManager.selectCard(0)
        assertNull(gameManager.selectedCardIndex)
    }
}

