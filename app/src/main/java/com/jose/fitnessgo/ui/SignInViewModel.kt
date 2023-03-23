package com.jose.fitnessgo.ui

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SignInViewModel : ViewModel() {

    private val _error = MutableStateFlow<Error?>(null)
    val error: StateFlow<Error?> = _error
    private val _requestFocus = MutableSharedFlow<FocusableField?>()
    val requestFocus: Flow<FocusableField?> = _requestFocus
    private val _loading = MutableStateFlow(false)
    val loading: Flow<Boolean> = _loading

    fun logInUser(
        email: String,
        password: String,
        doOnSuccess: () -> Unit,
        doOnFailure: (Exception) -> Unit,
    ) {
        if (email.isEmpty()) {
            _error.value = Error.EmailRequired
            _requestFocus.tryEmit(FocusableField.Email)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = Error.EmailInvalid
            _requestFocus.tryEmit(FocusableField.Email)
        } else if (password.isEmpty()) {
            _error.value = Error.PasswordRequired
            _requestFocus.tryEmit(FocusableField.Password)
        } else if (password.length < PasswordMinLength) {
            _error.value = Error.PasswordNotLongEnough
            _requestFocus.tryEmit(FocusableField.Password)
        } else {
            _loading.value = true
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    doOnSuccess()
                }.addOnFailureListener {
                    doOnFailure(it)
                }.addOnCompleteListener {
                    _loading.value = false
                }
        }
    }
}

enum class Error {
    EmailRequired,
    EmailInvalid,
    PasswordRequired,
    PasswordNotLongEnough
}

enum class FocusableField {
    Email,
    Password
}

private const val PasswordMinLength = 8
