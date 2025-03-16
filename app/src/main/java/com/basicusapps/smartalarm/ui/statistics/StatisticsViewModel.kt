package com.basicusapps.smartalarm.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basicusapps.smartalarm.data.model.MissionType
import com.basicusapps.smartalarm.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _statistics = MutableStateFlow(StatisticsState())
    val statistics: StateFlow<StatisticsState> = _statistics.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val totalAlarms = statisticsRepository.getTotalAlarms()
            val successRate = statisticsRepository.getSuccessRate()
            val avgCompletionTime = statisticsRepository.getAverageCompletionTime()
            
            val missionStats = MissionType.values()
                .filterNot { it == MissionType.NONE }
                .map { missionType ->
                    MissionStatistics(
                        missionType = missionType,
                        successRate = statisticsRepository.getSuccessRateForMission(missionType) ?: 0.0,
                        usageCount = 0 // TODO: Implement mission usage count
                    )
                }

            _statistics.value = StatisticsState(
                totalAlarms = totalAlarms,
                successfulWakeups = (totalAlarms * successRate).toInt(),
                successRate = successRate,
                averageCompletionTime = avgCompletionTime,
                missionStats = missionStats
            )
        }
    }
}

data class StatisticsState(
    val totalAlarms: Int = 0,
    val successfulWakeups: Int = 0,
    val successRate: Float = 0f,
    val averageCompletionTime: Double = 0.0,
    val missionStats: List<MissionStatistics> = emptyList()
)

data class MissionStatistics(
    val missionType: MissionType,
    val successRate: Double,
    val usageCount: Int
) 