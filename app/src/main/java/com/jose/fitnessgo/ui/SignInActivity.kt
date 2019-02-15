package com.jose.fitnessgo.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*



class SignInActivity : AppCompatActivity() {

    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.jose.fitnessgo.R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()
    }

    fun gotoSignUp(view: View){
        startActivity(Intent(this,SignUpActivity::class.java))
    }

    fun signInOnClick(view: View){
        userLogin()
    }

    fun userLogin(){
        val email = etEmailSignIn.text.toString().trim()
        val password = etPasswordSignIn.text.toString().trim()

        if(email.isEmpty()){
            etEmailSignIn.setError("Email is required")
            etEmailSignIn.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmailSignIn.setError("Please enter a valid email")
            etEmailSignIn.requestFocus()
            return
        }

        if(password.isEmpty()){
            etPasswordSignIn.setError("Password is required")
            etPasswordSignIn.requestFocus()
            return
        }

        if(password.length < 8){
            etPasswordSignIn.setError("Password is required")
            etPasswordSignIn.requestFocus()
            return
        }

        pbSignIn.visibility = View.VISIBLE

        mAuth?.signInWithEmailAndPassword(email,password)
                ?.addOnSuccessListener {
                    //Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()
                    pbSignIn.visibility = View.GONE

                    val loginIntent = Intent(this, MainActivity::class.java)
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(loginIntent)

                }
                ?.addOnFailureListener {
                    Toast.makeText(this, "Login unsuccessful: " +
                            "${it.message}", Toast.LENGTH_LONG).show()
                    pbSignIn.visibility = View.GONE
                }


    }

}
