package se2.hanabi.app.logic

sealed class PlaceCardResult(val type: PlaceCardResultType) {
    object Valid : PlaceCardResult(PlaceCardResultType.Valid)
    data class InvalidMove(val expected: Int) : PlaceCardResult(PlaceCardResultType.InvalidMove)
    object StackFull : PlaceCardResult(PlaceCardResultType.StackFull)
    object StackCompleted : PlaceCardResult(PlaceCardResultType.StackCompleted)
    object GameWon : PlaceCardResult(PlaceCardResultType.GameWon)
    object InvalidSelection : PlaceCardResult(PlaceCardResultType.InvalidSelection)
}