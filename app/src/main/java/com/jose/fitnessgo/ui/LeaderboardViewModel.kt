package com.jose.fitnessgo.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jose.fitnessgo.LeaderboardEntry
import com.jose.fitnessgo.data.firebase.FirestoreHelper

class LeaderboardViewModel : ViewModel() {

    val leaderboardList = MutableLiveData<List<LeaderboardEntry>>()

    fun loadAllUsers(doOnComplete: () -> Unit) {
        FirestoreHelper.loadAllUsers(
                { userProfiles ->
                    val descendingUserProfiles = userProfiles.toList().sortedByDescending { it.points }
                    val leaderboard = mutableListOf<LeaderboardEntry>()
                    for (userProfile in descendingUserProfiles) {
                        leaderboard.add(LeaderboardEntry(userProfile.name, userProfile.points))
                    }
                    leaderboardList.postValue(leaderboard)
                },
                {
                    doOnComplete()
                })
    }
}