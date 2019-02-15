package com.jose.fitnessgo.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        FirebaseApp.initializeApp(applicationContext)

        mAuth = FirebaseAuth.getInstance()
    }

    fun signUpOnClick(view: View){
        registerUser()
    }

    fun registerUser(){
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if(email.isEmpty()){
            etEmail.setError("Email is required")
            etEmail.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Please enter a valid email")
            etEmail.requestFocus()
            return
        }

        if(password.isEmpty()){
            etPassword.setError("Password is required")
            etPassword.requestFocus()
            return
        }

        if(password.length < 8){
            etPassword.setError("Password is required")
            etPassword.requestFocus()
            return
        }

        pbSignUp.visibility = View.VISIBLE

        mAuth?.createUserWithEmailAndPassword(email,password)
                ?.addOnSuccessListener {
                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_LONG).show()
                    pbSignUp.visibility = View.GONE
        }
                ?.addOnFailureListener {
                    Toast.makeText(this, "Registration unsuccessful: " +
                            "${it.toString().subSequence(
                                    // Trims the type of the exception,
                                    // so that only the description remains
                                    it.toString().indexOf(':') + 2,
                                    it.toString().length)
                            }", Toast.LENGTH_LONG).show()
                    pbSignUp.visibility = View.GONE
        }


    }



}
