package com.jose.fitnessgo.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

class SignInViewModel: ViewModel() {


    fun logInUser(email: String, password: String,
                         doOnSuccess: () -> Unit, doOnFailure: (Exception) -> Unit, doOnComplete: () -> Unit) {

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    doOnSuccess()
                }.addOnFailureListener {
                    doOnFailure(it)
                }.addOnCompleteListener {
                    doOnComplete()
                }
    }
}