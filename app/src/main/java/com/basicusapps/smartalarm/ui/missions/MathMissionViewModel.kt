package com.basicusapps.smartalarm.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basicusapps.smartalarm.data.repository.AlarmRepository
import com.basicusapps.smartalarm.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MathMissionViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    fun onMissionCompleted(alarmId: Long) {
        viewModelScope.launch {
            // Update alarm status
            val alarm = alarmRepository.getAlarmById(alarmId)
            alarm?.let {
                // Record successful completion in statistics
                statisticsRepository.recordSuccessfulWakeUp(
                    alarmId = alarmId,
                    completionTime = LocalDateTime.now(),
                    missionType = it.missionType,
                    attemptsUsed = 3 // We'll implement proper attempt tracking later
                )
            }
        }
    }
} 