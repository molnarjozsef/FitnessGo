package com.jose.fitnessgo.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v: View = inflater.inflate(R.layout.fragment_home, container, false)

        return v
    }

    override fun onStart() {
        cvGameStart.setOnClickListener {
            //val intent = Intent(activity, MapsActivity::class.java)
            //startActivity(intent)
            activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.addToBackStack(null)
                    ?.replace(com.jose.fitnessgo.R.id.fragment_container, MapsFragment())
                    ?.commit()
        }
        cvLeaderBoard.setOnClickListener {
            activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.addToBackStack(null)
                    ?.replace(com.jose.fitnessgo.R.id.fragment_container, LeaderboardFragment())
                    ?.commit()
        }
        cvProfileSettings.setOnClickListener {
            activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.addToBackStack(null)
                    ?.replace(com.jose.fitnessgo.R.id.fragment_container, ProfileSettingsFragment())
                    ?.commit()
        }

        super.onStart()
    }



}