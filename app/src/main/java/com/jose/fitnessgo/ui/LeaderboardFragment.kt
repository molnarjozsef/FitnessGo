package com.jose.fitnessgo.ui

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.FirebaseFirestore
import com.jose.fitnessgo.LeaderboardEntry
import com.jose.fitnessgo.MarginItemDecoration
import com.jose.fitnessgo.R
import com.jose.fitnessgo.adapter.LeaderboardAdapter
import com.jose.fitnessgo.data.firebase.FirestoreHelper
import kotlinx.android.synthetic.main.fragment_leaderboard.*


class LeaderboardFragment : Fragment() {

    private val viewModel by lazy {ViewModelProviders.of(this).get(LeaderboardViewModel::class.java)}


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pbLeaderBoard.visibility = View.VISIBLE
        rvLeaderboard.adapter = LeaderboardAdapter()
        rvLeaderboard.addItemDecoration(MarginItemDecoration(
                resources.getDimension(R.dimen.default_padding).toInt()))

        val leaderboardAdapter = LeaderboardAdapter()

        val leaderboardObserver = Observer<List<LeaderboardEntry>>{leaderBoardEntries ->
            for (entry in leaderBoardEntries){
                leaderboardAdapter.addItem(LeaderboardEntry(entry.name, entry.points))
            }
            rvLeaderboard.adapter = leaderboardAdapter
        }

        viewModel.leaderboardList.observe(this,leaderboardObserver)

        viewModel.loadAllUsers { pbLeaderBoard.visibility = View.GONE }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }


}
