package com.jose.fitnessgo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.jose.fitnessgo.LeaderboardEntry
import com.jose.fitnessgo.MarginItemDecoration
import com.jose.fitnessgo.R
import com.jose.fitnessgo.adapter.LeaderboardAdapter

class LeaderboardFragment : Fragment() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(LeaderboardViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.pbLeaderBoard).visibility = View.VISIBLE
        view.findViewById<RecyclerView>(R.id.rvLeaderboard).adapter = LeaderboardAdapter()
        view.findViewById<RecyclerView>(R.id.rvLeaderboard)
            .addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.default_padding).toInt()))

        val leaderboardAdapter = LeaderboardAdapter()

        val leaderboardObserver = Observer<List<LeaderboardEntry>> { leaderBoardEntries ->
            for (entry in leaderBoardEntries) {
                leaderboardAdapter.addItem(LeaderboardEntry(entry.name, entry.points))
            }
            view.findViewById<RecyclerView>(R.id.rvLeaderboard).adapter = leaderboardAdapter
        }

        viewModel.leaderboardList.observe(this, leaderboardObserver)

        viewModel.loadAllUsers { view.findViewById<View>(R.id.pbLeaderBoard).visibility = View.GONE }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.leaderboardList.removeObservers(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

}
