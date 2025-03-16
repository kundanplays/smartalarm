package com.basicusapps.smartalarm.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basicusapps.smartalarm.data.model.Alarm
import com.basicusapps.smartalarm.data.model.MissionDifficulty
import com.basicusapps.smartalarm.data.model.MissionType
import com.basicusapps.smartalarm.data.repository.AlarmRepository
import com.basicusapps.smartalarm.manager.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class AddEditAlarmViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _alarm = MutableStateFlow<Alarm?>(null)
    val alarm: StateFlow<Alarm?> = _alarm

    fun loadAlarm(alarmId: Long) {
        viewModelScope.launch {
            _alarm.value = repository.getAlarmById(alarmId)
        }
    }

    fun saveAlarm(
        hour: Int,
        minute: Int,
        label: String,
        repeatDays: Set<DayOfWeek>,
        missionType: MissionType,
        missionDifficulty: MissionDifficulty,
        vibrate: Boolean,
        snoozeEnabled: Boolean
    ) {
        viewModelScope.launch {
            val currentAlarm = _alarm.value
            val newAlarm = Alarm(
                id = currentAlarm?.id ?: 0,
                hour = hour,
                minute = minute,
                label = label,
                repeatDays = repeatDays,
                missionType = missionType,
                missionDifficulty = missionDifficulty,
                vibrate = vibrate,
                snoozeEnabled = snoozeEnabled
            )

            val alarmId = if (currentAlarm == null) {
                repository.insertAlarm(newAlarm)
            } else {
                repository.updateAlarm(newAlarm)
                currentAlarm.id
            }

            // Schedule the alarm
            alarmScheduler.schedule(newAlarm.copy(id = alarmId))
        }
    }

    fun deleteAlarm() {
        viewModelScope.launch {
            _alarm.value?.let { alarm ->
                repository.deleteAlarm(alarm)
                alarmScheduler.cancel(alarm)
            }
        }
    }
} 