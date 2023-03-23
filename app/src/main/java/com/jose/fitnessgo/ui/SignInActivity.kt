package com.jose.fitnessgo.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.jose.fitnessgo.R
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(SignInViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        findViewById<View>(R.id.btnSignIn).setOnClickListener {
            userLogin()
        }

        findViewById<View>(R.id.tvGotoSignUp).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        val etEmailSignIn = findViewById<TextInputLayout>(R.id.etEmailSignIn)
        val etPasswordSignIn = findViewById<TextInputLayout>(R.id.etPasswordSignIn)
        val pbSignIn = findViewById<ProgressBar>(R.id.pbSignIn)

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                when (error) {
                    Error.EmailRequired,
                    Error.EmailInvalid,
                    -> etEmailSignIn.error = this@SignInActivity.getString(error.errorTextRes())
                    Error.PasswordRequired,
                    Error.PasswordNotLongEnough,
                    -> etPasswordSignIn.error = this@SignInActivity.getString(error.errorTextRes())
                    null -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.requestFocus.collect { requestFocus ->
                when (requestFocus) {
                    FocusableField.Email -> etEmailSignIn.requestFocus()
                    FocusableField.Password -> etPasswordSignIn.requestFocus()
                    null -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.loading.collect { loading ->
                pbSignIn.isVisible = loading
            }
        }
    }

    private fun userLogin() {
        val etEmailSignIn = findViewById<TextInputLayout>(R.id.etEmailSignIn)
        val etPasswordSignIn = findViewById<TextInputLayout>(R.id.etPasswordSignIn)
        val clSignin = findViewById<View>(R.id.clSignin)

        val email = etEmailSignIn.editText?.text.toString().trim()
        val password = etPasswordSignIn.editText?.text.toString().trim()


        viewModel.logInUser(
            email = email,
            password = password,
            doOnSuccess = {
                val loginIntent = Intent(this, MainActivity::class.java)
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                loginIntent.putExtra("KEY_MESSAGE", "Login successful")
                startActivity(loginIntent)
            },
            doOnFailure = {
                Snackbar.make(
                    clSignin, "Login unsuccessful: " +
                        "${it.message}", Snackbar.LENGTH_LONG
                ).show()
            },
        )
    }

}

private fun Error.errorTextRes() = when (this) {
    Error.EmailRequired -> R.string.email_is_required
    Error.EmailInvalid -> R.string.enter_a_valid_email
    Error.PasswordRequired -> R.string.pw_is_required
    Error.PasswordNotLongEnough -> R.string.pw_must_be_at_least_8_chars
}
