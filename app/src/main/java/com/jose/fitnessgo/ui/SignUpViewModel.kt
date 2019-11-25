package com.jose.fitnessgo.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

class SignUpViewModel : ViewModel() {

    fun registerUserInDb(email: String, password: String,
                         doOnSuccess: () -> Unit, doOnFailure: (Exception) -> Unit, doOnComplete: () -> Unit) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    doOnSuccess()
                }.addOnFailureListener {
                    doOnFailure(it)

                }.addOnCompleteListener {
                    doOnComplete()
                }
    }
}