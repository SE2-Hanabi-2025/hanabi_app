package se2.hanabi.app.gamePlayUI

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import se2.hanabi.app.card.Card

class GamePlayViewModel: ViewModel() {

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: MutableStateFlow<Card?> = _selectedCard

    private val _selectedHandIndex = MutableStateFlow(-1)
    val selectedHandIndex: MutableStateFlow<Int> = _selectedHandIndex

    private val _selectedHint = MutableStateFlow("")
    val selectedHint: MutableStateFlow<String> = _selectedHint


    fun onPlayersCardClick(card: Card?) {
        _selectedHandIndex.value = -1
         _selectedCard.value = if (card == selectedCard.value) null else card
    }

    fun onOtherPlayersHandClick(handIndex: Int) {
        _selectedCard.value = null
        _selectedHandIndex.value = if (handIndex == selectedHandIndex.value) -1 else handIndex
    }

    fun onHintClick(hint: String) {
        _selectedHint.value = if (hint == selectedHint.value) "" else hint
    }

}