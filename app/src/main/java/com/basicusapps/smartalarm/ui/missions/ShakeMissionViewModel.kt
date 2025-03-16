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
import kotlin.math.sqrt

@HiltViewModel
class ShakeMissionViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _shakeState = MutableStateFlow(ShakeState())
    val shakeState: StateFlow<ShakeState> = _shakeState.asStateFlow()

    private var timerJob: Job? = null
    private var requiredShakeIntensity = 0f
    private var totalShakeIntensity = 0f

    fun startMission(difficulty: MissionDifficulty) {
        requiredShakeIntensity = when (difficulty) {
            MissionDifficulty.EASY -> 50f
            MissionDifficulty.MEDIUM -> 100f
            MissionDifficulty.HARD -> 200f
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _shakeState.value = _shakeState.value.copy(
                    elapsedSeconds = _shakeState.value.elapsedSeconds + 1
                )
            }
        }
    }

    fun onSensorChanged(x: Float, y: Float, z: Float) {
        val acceleration = sqrt(x * x + y * y + z * z)
        val shakeIntensity = acceleration - 9.81f // Subtract gravity
        
        if (shakeIntensity > 2f) { // Threshold to avoid small movements
            totalShakeIntensity += shakeIntensity
            updateProgress()
        }
    }

    private fun updateProgress() {
        val progress = ((totalShakeIntensity / requiredShakeIntensity) * 100).toInt().coerceIn(0, 100)
        _shakeState.value = _shakeState.value.copy(progress = progress)
        
        if (progress >= 100 && !_shakeState.value.isComplete) {
            _shakeState.value = _shakeState.value.copy(isComplete = true)
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
                    attemptsUsed = shakeState.value.elapsedSeconds
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

data class ShakeState(
    val progress: Int = 0,
    val elapsedSeconds: Int = 0,
    val isComplete: Boolean = false
) 