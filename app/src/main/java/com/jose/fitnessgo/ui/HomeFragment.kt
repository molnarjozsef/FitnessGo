package com.jose.fitnessgo.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v: View = inflater.inflate(R.layout.activity_home, container, false)

        return v
    }

    override fun onStart() {
        gameStart.setOnClickListener {
            val intent = Intent(activity, MapsActivity::class.java)
            startActivity(intent)
        }
        val mAuth = FirebaseAuth.getInstance()
        tvUserSettings.text = mAuth.currentUser?.email.toString()
        super.onStart()
    }



}