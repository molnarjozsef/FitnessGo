package com.jose.fitnessgo.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.fragment_profile_settings.*


class ProfileSettingsFragment : Fragment() {


    private val viewModel by lazy { ViewModelProviders.of(this).get(ProfileSettingsViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSetUserName.setOnClickListener {
            val user = HashMap<String, Any>()
            user["email"] = FirebaseAuth.getInstance().currentUser?.email.toString()
            user["name"] = etUsername.text.toString()


            viewModel.updateUsername(user, doOnSuccess = {
                Log.d("TAG_PROFILE_SETTINGS", "Added")
                val navigationView: NavigationView? = activity?.findViewById(R.id.nav_view)
                val headerView: View? = navigationView?.getHeaderView(0)
                val tvUserName: TextView? = headerView?.findViewById(R.id.tvUserName)
                tvUserName?.text = etUsername.text.toString()
            }, doOnFailure = { e ->
                Log.w("TAG_PROFILE_SETTINGS", "Error adding document", e)

            })

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_settings, container, false)
    }


}
