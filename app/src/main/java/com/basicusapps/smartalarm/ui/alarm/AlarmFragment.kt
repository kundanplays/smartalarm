package com.basicusapps.smartalarm.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.basicusapps.smartalarm.R
import com.basicusapps.smartalarm.databinding.FragmentAlarmBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmFragment : Fragment() {

    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlarmListViewModel by viewModels()
    private lateinit var alarmAdapter: AlarmAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeAlarms()
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onAlarmToggled = { alarm, isEnabled ->
                viewModel.toggleAlarm(alarm, isEnabled)
            },
            onAlarmClicked = { alarm ->
                findNavController().navigate(
                    AlarmFragmentDirections.actionAlarmToAddAlarm(alarm.id)
                )
            }
        )

        binding.alarmList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alarmAdapter
        }
    }

    private fun setupFab() {
        binding.addAlarmFab.setOnClickListener {
            findNavController().navigate(
                AlarmFragmentDirections.actionAlarmToAddAlarm(-1L)
            )
        }
    }

    private fun observeAlarms() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.alarms.collect { alarms ->
                alarmAdapter.submitList(alarms)
                updateEmptyState(alarms.isEmpty())
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.alarmList.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 