package com.jose.fitnessgo.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.view.View
import android.widget.Toast
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

    private fun registerUser(){
        val email = etEmailSignUp.editText?.text.toString().trim()
        val password = etPasswordSignUp.editText?.text.toString().trim()

        if(email.isEmpty()){
            etEmailSignUp.error = getString(R.string.email_is_required)
            etEmailSignUp.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmailSignUp.error = getString(R.string.enter_a_valid_email)
            etEmailSignUp.requestFocus()
            return
        }

        if(password.isEmpty()){
            etPasswordSignUp.error = getString(R.string.pw_is_required)
            etPasswordSignUp.requestFocus()
            return
        }

        if(password.length < 8){
            etPasswordSignUp.error = getString(R.string.pw_must_be_at_least_8_chars)
            etPasswordSignUp.requestFocus()
            return
        }

        pbSignUp.visibility = View.VISIBLE

        mAuth?.createUserWithEmailAndPassword(email,password)
                ?.addOnSuccessListener {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_LONG).show()

                    val loginIntent = Intent(this, MainActivity::class.java)
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(loginIntent)
                }
                ?.addOnFailureListener {
                    Toast.makeText(this, "Registration unsuccessful: " +
                            "${it.message}", Toast.LENGTH_LONG).show()
                }
                ?.addOnCompleteListener {
                    pbSignUp.visibility = View.GONE
                }


    }



}
