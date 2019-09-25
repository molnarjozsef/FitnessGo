package com.jose.fitnessgo.data.firebase

import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.jose.fitnessgo.LeaderboardEntry
import com.jose.fitnessgo.UserProfile
import kotlinx.android.synthetic.main.fragment_leaderboard.*
import java.util.HashMap

object FirestoreHelper {


    fun loadAllUsers(
            doOnSuccess: (ArrayList<UserProfile>) -> Unit,
            doOnComplete: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val users = arrayListOf<UserProfile>()
        db.collection("users").get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        users.add(UserProfile(
                                document.get("email").toString(),
                                (document.get("name")
                                        ?: document.get("email")).toString().substringBefore('@'),
                                Integer.parseInt(document.get("points")?.toString() ?: "0")
                        ))
                    }
                    doOnSuccess(users)
                }.addOnFailureListener { exception ->
                    Log.w("TAG_LEADERBOARD", "Error getting documents.", exception)
                }.addOnCompleteListener {
                    doOnComplete.invoke()
                }

    }

    fun loadUserData(
            email: String,
            doOnSuccess: (UserProfile) -> Unit,
            doOnComplete: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        var user: UserProfile
        db.collection("users")
                .document(email).get()
                .addOnSuccessListener { result ->
                    user = UserProfile(
                            result.get("email").toString(),
                            (result.get("name") ?: result.get("email")).toString().substringBefore('@'),
                            result.get("points").toString().toInt())

                    doOnSuccess(user)
                }.addOnFailureListener { exception ->
                    Log.w("TAG_LEADERBOARD", "Error getting document.", exception)
                }.addOnCompleteListener {
                    doOnComplete.invoke()
                }

    }

    fun saveUserData(email: String, points: Int){
        val db = FirebaseFirestore.getInstance()

        val userPtsData = HashMap<String, Any>()
        userPtsData["email"] = email
        userPtsData["points"] = points

        db.collection("users")
                .document(email)
                .update(userPtsData)
    }
}