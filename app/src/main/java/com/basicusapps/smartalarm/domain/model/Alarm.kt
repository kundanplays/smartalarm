package com.basicusapps.smartalarm.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Alarm(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "",
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val missionType: MissionType = MissionType.NONE,
    val missionDifficulty: MissionDifficulty = MissionDifficulty.EASY,
    val soundUri: String = "",
    val volume: Int = 100,
    val vibrate: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val snoozeDuration: Int = 5,
    val snoozeCount: Int = 3
) {
    fun getTime(): LocalTime = LocalTime.of(hour, minute)
} 