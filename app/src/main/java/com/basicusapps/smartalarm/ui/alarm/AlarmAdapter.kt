package com.basicusapps.smartalarm.ui.alarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.basicusapps.smartalarm.data.model.Alarm
import com.basicusapps.smartalarm.databinding.ItemAlarmBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class AlarmAdapter(
    private val onAlarmToggled: (Alarm, Boolean) -> Unit,
    private val onAlarmClicked: (Alarm) -> Unit
) : ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlarmViewHolder(
        private val binding: ItemAlarmBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAlarmClicked(getItem(position))
                }
            }

            binding.alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAlarmToggled(getItem(position), isChecked)
                }
            }
        }

        fun bind(alarm: Alarm) {
            binding.apply {
                timeText.text = formatTime(alarm.hour, alarm.minute)
                labelText.text = alarm.label
                repeatDaysText.text = formatRepeatDays(alarm.repeatDays)
                alarmSwitch.isChecked = alarm.isEnabled
                missionText.text = formatMissionType(alarm.missionType)
            }
        }

        private fun formatTime(hour: Int, minute: Int): String {
            return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        }

        private fun formatRepeatDays(days: Set<DayOfWeek>): String {
            if (days.isEmpty()) return "Once"
            return days.joinToString(", ") { it.name.take(3) }
        }

        private fun formatMissionType(type: MissionType): String {
            return when (type) {
                MissionType.NONE -> ""
                else -> type.name.lowercase().capitalize()
            }
        }
    }

    private class AlarmDiffCallback : DiffUtil.ItemCallback<Alarm>() {
        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem == newItem
        }
    }
} 