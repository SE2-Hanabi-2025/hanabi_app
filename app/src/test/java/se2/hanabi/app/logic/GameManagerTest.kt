package se2.hanabi.app.logic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import se2.hanabi.app.logic.PlaceCardResultType
import se2.hanabi.app.logic.PlaceCardResult


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

    @Test
    fun discardSelectedCard_removeSelectedCard() {
        gameManager.drawCard("3")
        gameManager.selectCard(0)
        val result = gameManager.discardSelectedCard()
        assertTrue(result)
        assertTrue(gameManager.drawnCards.isEmpty())
        assertNull(gameManager.selectedCardIndex)
    }

    @Test
    fun discardSelectedCard_doesNothingIfNoSelection() {
        val result = gameManager.discardSelectedCard()
        assertFalse(result)
    }

    @Test
    fun placeCardInBox_returnsInvalidMoveForWrongNumber() {
        gameManager.drawCard("2")
        gameManager.selectCard(0)
        val result = gameManager.placeCardInBox(0)
        assertTrue(result is PlaceCardResult.InvalidMove)
    }

    @Test
    fun placeCardInBox_returnStackFullIfBoxHas5() {
        gameManager.boxes[0] = "5"
        gameManager.drawCard("1")
        gameManager.selectCard(0)
        val result = gameManager.placeCardInBox(0)
        assertEquals(PlaceCardResultType.StackFull, result.type)
    }

    @Test
    fun placeCardInBox_returnsStackCompletedWhenPlacing5NotGameWon() {
        gameManager.drawCard("1")
        gameManager.selectCard(0)
        gameManager.placeCardInBox(0)

        gameManager.drawCard("2")
        gameManager.selectCard(0)
        gameManager.placeCardInBox(0)

        gameManager.drawCard("3")
        gameManager.selectCard(0)
        gameManager.placeCardInBox(0)

        gameManager.drawCard("4")
        gameManager.selectCard(0)
        gameManager.placeCardInBox(0)

        gameManager.drawCard("5")
        gameManager.selectCard(0)
        val result = gameManager.placeCardInBox(0)
        assertEquals(PlaceCardResultType.StackCompleted, result.type)
    }

    @Test
    fun placeCardInBox_returnsGameWonWhenAllBoxesAre5() {
        repeat(4) {
            gameManager.boxes[it] = "5"
        }
        gameManager.drawCard("1")
        gameManager.selectCard(0)
        gameManager.placeCardInBox(4)

        gameManager.drawCard("2")
        gameManager.selectCard(0)
        gameManager.placeCardInBox(4)

        gameManager.drawCard("3")
        gameManager.selectCard(0)
        gameManager.placeCardInBox(4)

        gameManager.drawCard("4")
        gameManager.selectCard(0)
        gameManager.placeCardInBox(4)

        gameManager.drawCard("5")
        gameManager.selectCard(0)
        val result = gameManager.placeCardInBox(4)
        assertEquals(PlaceCardResultType.GameWon, result.type)
    }

    @Test
    fun placeCardInBox_returnsInvalidSelectionWhenNoCardSelected() {
        val result = gameManager.placeCardInBox(0)
        assertEquals(PlaceCardResultType.InvalidSelection, result.type)
    }

}

