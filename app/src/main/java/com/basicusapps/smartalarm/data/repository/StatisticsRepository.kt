package com.basicusapps.smartalarm.data.repository

import com.basicusapps.smartalarm.data.dao.StatisticsDao
import com.basicusapps.smartalarm.data.model.AlarmStatistics
import com.basicusapps.smartalarm.domain.model.MissionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val statisticsDao: StatisticsDao
) {
    fun getAllStatistics(): Flow<List<AlarmStatistics>> = statisticsDao.getAllStatistics()

    fun getStatisticsByAlarmId(alarmId: Long): Flow<List<AlarmStatistics>> =
        statisticsDao.getStatisticsByAlarmId(alarmId)

    suspend fun recordSuccessfulWakeUp(
        alarmId: Long,
        completionTime: LocalDateTime,
        missionType: MissionType,
        attemptsUsed: Int
    ) {
        val statistics = AlarmStatistics(
            alarmId = alarmId,
            completionTime = completionTime,
            missionType = missionType,
            attemptsUsed = attemptsUsed,
            isSuccessful = true
        )
        statisticsDao.insert(statistics)
    }

    suspend fun recordMissedAlarm(
        alarmId: Long,
        missionType: MissionType
    ) {
        val statistics = AlarmStatistics(
            alarmId = alarmId,
            completionTime = LocalDateTime.now(),
            missionType = missionType,
            attemptsUsed = 0,
            isSuccessful = false
        )
        statisticsDao.insert(statistics)
    }

    suspend fun getSuccessRate(): Float {
        val total = statisticsDao.getTotalAlarms()
        if (total == 0) return 0f
        val successful = statisticsDao.getSuccessfulAlarms()
        return successful.toFloat() / total
    }

    suspend fun getAverageCompletionTime(): Double {
        return statisticsDao.getAverageCompletionTime() ?: 0.0
    }

    suspend fun getMostUsedMissionType(): MissionType? {
        return statisticsDao.getMostUsedMissionType()
    }
} 