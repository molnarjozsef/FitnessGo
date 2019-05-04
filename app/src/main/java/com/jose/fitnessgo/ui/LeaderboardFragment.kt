package com.jose.fitnessgo.ui

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.jose.fitnessgo.LeaderboardEntry
import com.jose.fitnessgo.R
import com.jose.fitnessgo.adapter.LeaderboardAdapter
import kotlinx.android.synthetic.main.fragment_leaderboard.*


class LeaderboardFragment : Fragment() {


    private var db: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pbLeaderBoard.visibility = View.VISIBLE
        rvLeaderboard.adapter = LeaderboardAdapter()

        val leaderboardEntries = hashMapOf<String, Int>()


        val leaderboardAdapter = LeaderboardAdapter()

        db?.collection("users")
                ?.get()
                ?.addOnSuccessListener { result ->
                    for (document in result) {
                        leaderboardEntries[(document.get("name")
                                ?: document.get("email")).toString().substringBefore('@')] =
                                Integer.parseInt(document.get("points")?.toString() ?: "0")
                    }
                    val leaderBoardResult = leaderboardEntries.toList().sortedByDescending { (_, value) -> value }.toMap()

                    for (entry in leaderBoardResult) {
                        leaderboardAdapter.addItem(LeaderboardEntry(entry.key, entry.value))
                    }

                    rvLeaderboard.adapter = leaderboardAdapter
                }
                ?.addOnFailureListener { exception ->
                    Log.w("TAG_LEADERBOARD", "Error getting documents.", exception)
                }
                ?.addOnCompleteListener {
                    pbLeaderBoard.visibility = View.GONE
                }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

}
