package com.jose.fitnessgo.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.jose.fitnessgo.R

class SignUpActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(SignUpViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        FirebaseApp.initializeApp(applicationContext)

        findViewById<View>(R.id.btnSignUp).setOnClickListener {
            registerUser()
        }

    }

    private fun registerUser() {
        val etEmailSignUp = findViewById<TextInputLayout>(R.id.etEmailSignUp)
        val etPasswordSignUp = findViewById<TextInputLayout>(R.id.etPasswordSignUp)
        val pbSignUp = findViewById<ProgressBar>(R.id.pbSignUp)
        val clSignup = findViewById<View>(R.id.clSignup)

        val email = etEmailSignUp.editText?.text.toString().trim()
        val password = etPasswordSignUp.editText?.text.toString().trim()

        if (email.isEmpty()) {
            etEmailSignUp.error = getString(R.string.email_is_required)
            etEmailSignUp.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailSignUp.error = getString(R.string.enter_a_valid_email)
            etEmailSignUp.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPasswordSignUp.error = getString(R.string.pw_is_required)
            etPasswordSignUp.requestFocus()
            return
        }

        if (password.length < 8) {
            etPasswordSignUp.error = getString(R.string.pw_must_be_at_least_8_chars)
            etPasswordSignUp.requestFocus()
            return
        }

        pbSignUp.visibility = View.VISIBLE

        viewModel.registerUserInDb(email, password,
            doOnSuccess = {
                val loginIntent = Intent(this, MainActivity::class.java)
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                loginIntent.putExtra("KEfY_MESSAGE", "Registration successful")
                startActivity(loginIntent)
            },
            doOnFailure = {
                Snackbar.make(
                    clSignup, "Registration unsuccessful: " +
                        "${it.message}", Snackbar.LENGTH_LONG
                ).show()
            },
            doOnComplete = {
                pbSignUp.visibility = View.GONE

            }
        )

    }

}
