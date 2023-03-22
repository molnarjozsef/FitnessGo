package com.jose.fitnessgo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jose.fitnessgo.R

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onStart() {
        view?.findViewById<View>(R.id.cvGameStart)?.setOnClickListener {
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.addToBackStack(null)
                ?.replace(R.id.fragment_container, MapsFragment())
                ?.commit()
        }
        view?.findViewById<View>(R.id.cvLeaderBoard)?.setOnClickListener {
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.addToBackStack(null)
                ?.replace(R.id.fragment_container, LeaderboardFragment())
                ?.commit()
        }
        view?.findViewById<View>(R.id.cvProfileSettings)?.setOnClickListener {
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.addToBackStack(null)
                ?.replace(R.id.fragment_container, ProfileSettingsFragment())
                ?.commit()
        }

        super.onStart()
    }
}
