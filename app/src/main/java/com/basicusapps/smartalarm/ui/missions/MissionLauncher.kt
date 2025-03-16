package com.basicusapps.smartalarm.ui.missions

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.basicusapps.smartalarm.data.model.Alarm
import com.basicusapps.smartalarm.data.model.MissionType
import javax.inject.Inject

class MissionLauncher @Inject constructor() {

    fun launchMission(fragment: Fragment, alarm: Alarm) {
        val navController = fragment.findNavController()
        val difficulty = alarm.missionDifficulty

        val action = when (alarm.missionType) {
            MissionType.MATH -> AlarmFragmentDirections.actionAlarmFragmentToMathMissionFragment(
                alarmId = alarm.id,
                difficulty = difficulty
            )
            MissionType.MEMORY -> AlarmFragmentDirections.actionAlarmFragmentToMemoryMissionFragment(
                alarmId = alarm.id,
                difficulty = difficulty
            )
            MissionType.SHAKE -> AlarmFragmentDirections.actionAlarmFragmentToShakeMissionFragment(
                alarmId = alarm.id,
                difficulty = difficulty
            )
            MissionType.BARCODE -> AlarmFragmentDirections.actionAlarmFragmentToBarcodeMissionFragment(
                alarmId = alarm.id,
                difficulty = difficulty
            )
            MissionType.STEPS -> AlarmFragmentDirections.actionAlarmFragmentToStepMissionFragment(
                alarmId = alarm.id,
                difficulty = difficulty
            )
            MissionType.NONE -> null
        }

        action?.let { navController.navigate(it) }
    }
} 