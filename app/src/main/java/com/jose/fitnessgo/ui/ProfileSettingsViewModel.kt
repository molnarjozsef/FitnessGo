package com.jose.fitnessgo.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileSettingsViewModel : ViewModel() {

    fun updateUsername(user: HashMap<String, Any>, doOnSuccess: () -> Unit, doOnFailure: (e: Exception) -> Unit) {
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.email.toString())
                .update(user)
                .addOnSuccessListener {
                    doOnSuccess()
                }
                .addOnFailureListener { e ->
                    doOnFailure(e)
                }
    }

}