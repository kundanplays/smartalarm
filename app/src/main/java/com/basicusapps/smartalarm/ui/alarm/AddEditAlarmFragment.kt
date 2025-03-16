package com.basicusapps.smartalarm.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.basicusapps.smartalarm.R
import com.basicusapps.smartalarm.databinding.FragmentAddEditAlarmBinding
import com.basicusapps.smartalarm.domain.model.Alarm
import com.basicusapps.smartalarm.domain.model.MissionDifficulty
import com.basicusapps.smartalarm.domain.model.MissionType
import com.google.android.material.chip.Chip
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

@AndroidEntryPoint
class AddEditAlarmFragment : Fragment() {
    private var _binding: FragmentAddEditAlarmBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddEditAlarmViewModel by viewModels()
    private val args: AddEditAlarmFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        
        if (args.alarmId != -1L) {
            viewModel.loadAlarm(args.alarmId)
        }
    }

    private fun setupUI() {
        setupTimePicker()
        setupMissionSpinners()
        setupButtons()
    }

    private fun setupTimePicker() {
        binding.timePicker.setOnClickListener {
            val currentTime = LocalTime.now()
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentTime.hour)
                .setMinute(currentTime.minute)
                .setTitleText(getString(R.string.alarm_time))
                .build()

            picker.addOnPositiveButtonClickListener {
                // Time will be handled when saving
            }

            picker.show(childFragmentManager, "time_picker")
        }
    }

    private fun setupMissionSpinners() {
        // Mission Type Spinner
        val missionTypes = MissionType.values().map { getString(getMissionTypeStringResource(it)) }
        val missionTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            missionTypes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.missionTypeSpinner.adapter = missionTypeAdapter

        // Mission Difficulty Spinner
        val difficulties = MissionDifficulty.values().map { getString(getDifficultyStringResource(it)) }
        val difficultyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            difficulties
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.missionDifficultySpinner.adapter = difficultyAdapter
    }

    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            saveAlarm()
        }

        binding.deleteButton.apply {
            visibility = if (args.alarmId != -1L) View.VISIBLE else View.GONE
            setOnClickListener {
                viewModel.deleteAlarm(args.alarmId)
                findNavController().navigateUp()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.alarm.collect { alarm ->
                    alarm?.let { updateUI(it) }
                }
            }
        }
    }

    private fun updateUI(alarm: Alarm) {
        binding.apply {
            alarmNameEditText.setText(alarm.label)
            
            // Set repeat days
            alarm.repeatDays.forEach { day ->
                val chipId = when (day) {
                    DayOfWeek.MONDAY -> R.id.mondayChip
                    DayOfWeek.TUESDAY -> R.id.tuesdayChip
                    DayOfWeek.WEDNESDAY -> R.id.wednesdayChip
                    DayOfWeek.THURSDAY -> R.id.thursdayChip
                    DayOfWeek.FRIDAY -> R.id.fridayChip
                    DayOfWeek.SATURDAY -> R.id.saturdayChip
                    DayOfWeek.SUNDAY -> R.id.sundayChip
                }
                repeatDaysChipGroup.findViewById<Chip>(chipId).isChecked = true
            }

            // Set mission type and difficulty
            missionTypeSpinner.setSelection(MissionType.values().indexOf(alarm.missionType))
            missionDifficultySpinner.setSelection(MissionDifficulty.values().indexOf(alarm.missionDifficulty))

            // Set switches
            vibrationSwitch.isChecked = alarm.vibration
            snoozeSwitch.isChecked = alarm.allowSnooze
        }
    }

    private fun saveAlarm() {
        val label = binding.alarmNameEditText.text.toString()
        
        val repeatDays = mutableSetOf<DayOfWeek>()
        binding.repeatDaysChipGroup.checkedChipIds.forEach { chipId ->
            when (chipId) {
                R.id.mondayChip -> repeatDays.add(DayOfWeek.MONDAY)
                R.id.tuesdayChip -> repeatDays.add(DayOfWeek.TUESDAY)
                R.id.wednesdayChip -> repeatDays.add(DayOfWeek.WEDNESDAY)
                R.id.thursdayChip -> repeatDays.add(DayOfWeek.THURSDAY)
                R.id.fridayChip -> repeatDays.add(DayOfWeek.FRIDAY)
                R.id.saturdayChip -> repeatDays.add(DayOfWeek.SATURDAY)
                R.id.sundayChip -> repeatDays.add(DayOfWeek.SUNDAY)
            }
        }

        val missionType = MissionType.values()[binding.missionTypeSpinner.selectedItemPosition]
        val missionDifficulty = MissionDifficulty.values()[binding.missionDifficultySpinner.selectedItemPosition]
        
        val time = LocalTime.now() // This should be updated with the actual selected time from the TimePicker

        viewModel.saveAlarm(
            time = time,
            label = label,
            repeatDays = repeatDays,
            missionType = missionType,
            missionDifficulty = missionDifficulty,
            vibration = binding.vibrationSwitch.isChecked,
            allowSnooze = binding.snoozeSwitch.isChecked
        )

        findNavController().navigateUp()
    }

    private fun getMissionTypeStringResource(missionType: MissionType): Int {
        return when (missionType) {
            MissionType.NONE -> R.string.no_mission
            MissionType.MATH -> R.string.mission_math
            MissionType.MEMORY -> R.string.mission_memory
            MissionType.SHAKE -> R.string.mission_shake
            MissionType.BARCODE -> R.string.mission_barcode
            MissionType.STEPS -> R.string.mission_steps
        }
    }

    private fun getDifficultyStringResource(difficulty: MissionDifficulty): Int {
        return when (difficulty) {
            MissionDifficulty.EASY -> R.string.difficulty_easy
            MissionDifficulty.MEDIUM -> R.string.difficulty_medium
            MissionDifficulty.HARD -> R.string.difficulty_hard
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 