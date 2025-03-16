package com.basicusapps.smartalarm.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.basicusapps.smartalarm.R
import com.basicusapps.smartalarm.databinding.FragmentStatisticsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var missionStatsAdapter: MissionStatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeStatistics()
    }

    private fun setupRecyclerView() {
        missionStatsAdapter = MissionStatsAdapter()
        binding.missionStatsList.apply {
            adapter = missionStatsAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun observeStatistics() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.statistics.collectLatest { stats ->
                binding.apply {
                    totalAlarmsText.text = stats.totalAlarms.toString()
                    successfulWakeupsText.text = stats.successfulWakeups.toString()
                    successRateText.text = getString(
                        R.string.success_rate_format,
                        stats.successRate * 100
                    )
                    avgCompletionTimeText.text = getString(
                        R.string.completion_time_format,
                        stats.averageCompletionTime
                    )
                }
                missionStatsAdapter.submitList(stats.missionStats)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 