package com.jose.fitnessgo.data.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jose.fitnessgo.UserProfile

object FirestoreHelper {

    fun loadAllUsers(
        doOnSuccess: (ArrayList<UserProfile>) -> Unit,
        doOnComplete: () -> Unit,
    ) {
        val db = FirebaseFirestore.getInstance()
        val users = arrayListOf<UserProfile>()
        db.collection("users").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    users.add(
                        UserProfile(
                            document.get("email").toString(),
                            (document.get("name")
                                ?: document.get("email")).toString().substringBefore('@'),
                            Integer.parseInt(document.get("points")?.toString() ?: "0")
                        )
                    )
                }
                doOnSuccess(users)
            }
            .addOnFailureListener { exception ->
                Log.w("TAG_LEADERBOARD", "Error getting documents.", exception)
            }
            .addOnCompleteListener {
                doOnComplete.invoke()
            }

    }

    fun loadUserData(
        email: String,
        doOnSuccess: (UserProfile) -> Unit,
        doOnComplete: () -> Unit,
    ) {
        val db = FirebaseFirestore.getInstance()
        var user: UserProfile
        db.collection("users")
            .document(email).get()
            .addOnSuccessListener { result ->
                user = UserProfile(
                    result.get("email").toString(),
                    (result.get("name")
                        ?: result.get("email")).toString().substringBefore('@'),
                    result.get("points").toString().toInt()
                )

                doOnSuccess(user)
            }.addOnFailureListener { exception ->
                Log.w("TAG_LEADERBOARD", "Error getting document.", exception)
            }.addOnCompleteListener {
                doOnComplete.invoke()
            }

    }

    fun saveUserData(email: String, points: Int) {
        val db = FirebaseFirestore.getInstance()

        val userPtsData = HashMap<String, Any>()
        userPtsData["email"] = email
        userPtsData["points"] = points

        db.collection("users")
            .document(email)
            .update(userPtsData)
    }

    fun addIfNotAdded(email: String, doOnSuccess: (UserProfile) -> Unit, doOnFailure: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(email)
        docRef.get()
            .addOnSuccessListener { document ->
                val user: UserProfile
                if (document.exists()) {
                    user = UserProfile(
                        document.get("email").toString(),
                        (document.get("name")
                            ?: document.get("email")).toString().substringBefore('@'),
                        document.get("points").toString().toInt()
                    )

                    doOnSuccess(user)
                } else {
                    val userData = HashMap<String, Any>()
                    userData["email"] = email
                    db.collection("users")
                        .document(email)
                        .set(userData)

                    val userPtsData = HashMap<String, Any>()
                    userPtsData["email"] = email
                    userPtsData["points"] = 0
                    db.collection("users").document(email).update(userPtsData)

                    val userNameData = HashMap<String, Any>()
                    userNameData["email"] = email
                    userNameData["name"] = email.substringBefore('@')
                    db.collection("users").document(email).update(userNameData)

                    doOnSuccess(UserProfile(email, email.substringBefore('@'), 0))
                }
            }
            .addOnFailureListener {
                doOnFailure()
            }
    }
}
