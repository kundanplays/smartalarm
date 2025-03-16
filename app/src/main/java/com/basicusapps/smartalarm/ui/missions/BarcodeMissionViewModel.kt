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
class BarcodeMissionViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _scanState = MutableStateFlow(BarcodeScanState())
    val scanState: StateFlow<BarcodeScanState> = _scanState.asStateFlow()

    private var timerJob: Job? = null
    private var targetBarcode: String? = null

    fun startMission(difficulty: MissionDifficulty) {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _scanState.value = _scanState.value.copy(
                    elapsedSeconds = _scanState.value.elapsedSeconds + 1
                )
            }
        }
    }

    fun onBarcodeDetected(barcode: String) {
        if (_scanState.value.isComplete) return

        if (targetBarcode == null) {
            // First scan sets the target barcode
            targetBarcode = barcode
            _scanState.value = _scanState.value.copy(
                message = "Barcode saved! Scan it again to complete the mission."
            )
        } else if (barcode == targetBarcode) {
            // Second scan of the same barcode completes the mission
            _scanState.value = _scanState.value.copy(
                isComplete = true,
                message = "Mission complete!"
            )
            timerJob?.cancel()
        } else {
            _scanState.value = _scanState.value.copy(
                message = "Wrong barcode! Please scan the same barcode again."
            )
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
                    attemptsUsed = scanState.value.elapsedSeconds
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

data class BarcodeScanState(
    val elapsedSeconds: Int = 0,
    val isComplete: Boolean = false,
    val message: String = "Scan any barcode to begin"
) 