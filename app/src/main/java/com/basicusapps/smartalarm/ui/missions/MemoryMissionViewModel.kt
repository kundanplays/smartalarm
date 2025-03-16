package com.basicusapps.smartalarm.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basicusapps.smartalarm.data.repository.AlarmRepository
import com.basicusapps.smartalarm.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MemoryMissionViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(MemoryGameState())
    val gameState: StateFlow<MemoryGameState> = _gameState.asStateFlow()

    private var timerJob: Job? = null
    private var firstSelectedPosition: Int? = null
    private var secondSelectedPosition: Int? = null
    private var canFlipCards = true

    fun startGame(difficulty: MissionDifficulty) {
        val gridSize = when (difficulty) {
            MissionDifficulty.EASY -> 16
            MissionDifficulty.MEDIUM -> 25
            MissionDifficulty.HARD -> 36
        }
        val cards = generateCards(gridSize)
        _gameState.value = MemoryGameState(
            cards = cards,
            totalPairs = gridSize / 2
        )
        startTimer()
    }

    private fun generateCards(count: Int): List<MemoryCard> {
        val pairs = count / 2
        val symbols = (1..pairs).flatMap { listOf(it, it) }.shuffled()
        return symbols.mapIndexed { index, symbol ->
            MemoryCard(
                id = index,
                symbol = symbol,
                isFaceUp = false,
                isMatched = false
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _gameState.value = _gameState.value.copy(
                    elapsedSeconds = _gameState.value.elapsedSeconds + 1
                )
            }
        }
    }

    fun onCardClicked(position: Int) {
        if (!canFlipCards) return
        val currentState = _gameState.value
        val card = currentState.cards[position]

        if (card.isFaceUp || card.isMatched) return

        when {
            firstSelectedPosition == null -> {
                firstSelectedPosition = position
                flipCard(position, true)
            }
            secondSelectedPosition == null && position != firstSelectedPosition -> {
                secondSelectedPosition = position
                flipCard(position, true)
                checkForMatch()
            }
        }
    }

    private fun flipCard(position: Int, isFaceUp: Boolean) {
        val currentState = _gameState.value
        val updatedCards = currentState.cards.toMutableList()
        updatedCards[position] = updatedCards[position].copy(isFaceUp = isFaceUp)
        _gameState.value = currentState.copy(cards = updatedCards)
    }

    private fun checkForMatch() {
        canFlipCards = false
        viewModelScope.launch {
            delay(1000) // Give player time to memorize cards

            val firstCard = _gameState.value.cards[firstSelectedPosition!!]
            val secondCard = _gameState.value.cards[secondSelectedPosition!!]

            if (firstCard.symbol == secondCard.symbol) {
                markCardsAsMatched()
                checkGameCompletion()
            } else {
                flipCards(false)
            }

            firstSelectedPosition = null
            secondSelectedPosition = null
            canFlipCards = true
        }
    }

    private fun markCardsAsMatched() {
        val currentState = _gameState.value
        val updatedCards = currentState.cards.toMutableList()
        val positions = listOf(firstSelectedPosition!!, secondSelectedPosition!!)

        positions.forEach { position ->
            updatedCards[position] = updatedCards[position].copy(isMatched = true)
        }

        _gameState.value = currentState.copy(
            cards = updatedCards,
            pairsFound = currentState.pairsFound + 1
        )
    }

    private fun flipCards(isFaceUp: Boolean) {
        val currentState = _gameState.value
        val updatedCards = currentState.cards.toMutableList()
        val positions = listOf(firstSelectedPosition!!, secondSelectedPosition!!)

        positions.forEach { position ->
            updatedCards[position] = updatedCards[position].copy(isFaceUp = isFaceUp)
        }

        _gameState.value = currentState.copy(cards = updatedCards)
    }

    private fun checkGameCompletion() {
        val currentState = _gameState.value
        if (currentState.pairsFound == currentState.totalPairs) {
            _gameState.value = currentState.copy(isGameComplete = true)
            timerJob?.cancel()
        }
    }

    fun onMissionCompleted(alarmId: Long) {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            alarm?.let {
                statisticsRepository.recordSuccessfulWakeUp(
                    alarmId = alarmId,
                    completionTime = LocalDateTime.now(),
                    missionType = it.missionType,
                    attemptsUsed = gameState.value.elapsedSeconds.toInt()
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

data class MemoryGameState(
    val cards: List<MemoryCard> = emptyList(),
    val pairsFound: Int = 0,
    val totalPairs: Int = 0,
    val elapsedSeconds: Int = 0,
    val isGameComplete: Boolean = false
)

data class MemoryCard(
    val id: Int,
    val symbol: Int,
    val isFaceUp: Boolean,
    val isMatched: Boolean
) 