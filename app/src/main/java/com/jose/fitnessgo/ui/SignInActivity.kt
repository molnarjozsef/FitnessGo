package com.jose.fitnessgo.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.jose.fitnessgo.R
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
        val email = etEmailSignIn.editText?.text.toString().trim()
        val password = etPasswordSignIn.editText?.text.toString().trim()

        if(email.isEmpty()){
            etEmailSignIn.error = getString(R.string.email_is_required)
            etEmailSignIn.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmailSignIn.error = getString(R.string.enter_a_valid_email)
            etEmailSignIn.requestFocus()
            return
        }

        if(password.isEmpty()){
            etPasswordSignIn.error = getString(R.string.pw_is_required)
            etPasswordSignIn.requestFocus()
            return
        }

        if(password.length < 8){
            etPasswordSignIn.error = getString(R.string.pw_must_be_at_least_8_chars)
            etPasswordSignIn.requestFocus()
            return
        }

        pbSignIn.visibility = View.VISIBLE

        mAuth?.signInWithEmailAndPassword(email,password)
                ?.addOnSuccessListener {
                    //Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()

                    val loginIntent = Intent(this, MainActivity::class.java)
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    loginIntent.putExtra("KEY_MESSAGE","Login successful")
                    startActivity(loginIntent)

                }
                ?.addOnFailureListener {
                    Snackbar.make(clSignin, "Login unsuccessful: " +
                            "${it.message}", Snackbar.LENGTH_LONG).show()
                }
                ?.addOnCompleteListener {
                    pbSignIn.visibility = View.GONE
                }


    }

}
