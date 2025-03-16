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

@HiltViewModel
class StepMissionViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _stepState = MutableStateFlow(StepState())
    val stepState: StateFlow<StepState> = _stepState.asStateFlow()

    private var timerJob: Job? = null
    private var targetSteps = 0
    private var initialSteps = 0

    fun startMission(difficulty: MissionDifficulty) {
        targetSteps = when (difficulty) {
            MissionDifficulty.EASY -> 50
            MissionDifficulty.MEDIUM -> 100
            MissionDifficulty.HARD -> 200
        }
        _stepState.value = _stepState.value.copy(targetSteps = targetSteps)
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _stepState.value = _stepState.value.copy(
                    elapsedSeconds = _stepState.value.elapsedSeconds + 1
                )
            }
        }
    }

    fun setInitialSteps(steps: Int) {
        initialSteps = steps
    }

    fun onStepDetected(totalSteps: Int) {
        val currentSteps = totalSteps - initialSteps
        val progress = ((currentSteps.toFloat() / targetSteps) * 100).toInt().coerceIn(0, 100)

        _stepState.value = _stepState.value.copy(
            currentSteps = currentSteps,
            progress = progress
        )

        if (currentSteps >= targetSteps && !_stepState.value.isComplete) {
            _stepState.value = _stepState.value.copy(isComplete = true)
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
                    attemptsUsed = stepState.value.elapsedSeconds
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

data class StepState(
    val currentSteps: Int = 0,
    val targetSteps: Int = 0,
    val progress: Int = 0,
    val elapsedSeconds: Int = 0,
    val isComplete: Boolean = false
) 