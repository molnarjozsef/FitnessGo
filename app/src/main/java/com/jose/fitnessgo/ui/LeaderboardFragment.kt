package com.jose.fitnessgo.ui

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.fragment_leaderboard.*




/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LeaderBoardFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LeaderBoardFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class LeaderBoardFragment : Fragment() {


    private var db: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pbLeaderBoard.visibility = View.VISIBLE
        var leaderBoardEntries = hashMapOf<String, Int>()

        //leaderBoardEntries.put("asd","meow")
        //leaderBoardEntries.put("bsd","vau")

        db?.collection("users")
                ?.get()
                ?.addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d("TAG_LEADERBOARD", document.id + " => " + document.data)
                        leaderBoardEntries.put(
                                (document.get("name") ?: document.get("email")).toString(),
                                Integer.parseInt(document.get("points").toString())
                        )
                    }
                    val leaderBoardResult = leaderBoardEntries.toList().sortedByDescending { (_, value) -> value}.toMap()

                    for (entry in leaderBoardResult) {
                        tvLeaderBoard.text = "${tvLeaderBoard.text} ${entry.key}: ${entry.value}\n"
                    }
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
