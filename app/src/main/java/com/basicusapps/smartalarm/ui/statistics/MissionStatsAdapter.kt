package com.basicusapps.smartalarm.ui.statistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basicusapps.smartalarm.R
import com.basicusapps.smartalarm.databinding.ItemMissionStatisticsBinding
import com.basicusapps.smartalarm.data.model.MissionType

class MissionStatsAdapter : ListAdapter<MissionStatistics, MissionStatsAdapter.ViewHolder>(
    MissionStatsDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMissionStatisticsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemMissionStatisticsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stats: MissionStatistics) {
            val context = binding.root.context
            binding.apply {
                missionNameText.text = getMissionName(stats.missionType)
                missionSuccessText.text = context.getString(
                    R.string.mission_success_rate,
                    getMissionName(stats.missionType),
                    context.getString(R.string.success_rate_format, stats.successRate * 100)
                )
                missionUsageText.text = context.getString(
                    R.string.mission_usage_count,
                    stats.usageCount
                )
            }
        }

        private fun getMissionName(missionType: MissionType): String {
            return binding.root.context.getString(
                when (missionType) {
                    MissionType.MATH -> R.string.mission_math
                    MissionType.MEMORY -> R.string.mission_memory
                    MissionType.SHAKE -> R.string.mission_shake
                    MissionType.BARCODE -> R.string.mission_barcode
                    MissionType.STEPS -> R.string.mission_steps
                    MissionType.NONE -> R.string.no_mission
                }
            )
        }
    }
}

class MissionStatsDiffCallback : DiffUtil.ItemCallback<MissionStatistics>() {
    override fun areItemsTheSame(oldItem: MissionStatistics, newItem: MissionStatistics): Boolean {
        return oldItem.missionType == newItem.missionType
    }

    override fun areContentsTheSame(oldItem: MissionStatistics, newItem: MissionStatistics): Boolean {
        return oldItem == newItem
    }
} 