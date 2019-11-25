package com.jose.fitnessgo.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jose.fitnessgo.UserProfile
import com.jose.fitnessgo.data.firebase.FirebaseAuthHelper
import com.jose.fitnessgo.data.firebase.FirestoreHelper

class MainViewModel : ViewModel() {

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun addUserIfNotAdded(doOnSuccess: (UserProfile) -> Unit, doOnFailure: () -> Unit) {
        FirestoreHelper.addIfNotAdded(
                FirebaseAuthHelper.currentUser()?.email.toString(),
                doOnSuccess, doOnFailure
        )


    }
}