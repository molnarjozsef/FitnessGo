package com.jose.fitnessgo.ui

import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.fragment_profile_settings.*



class ProfileSettingsFragment : Fragment() {


    private var db: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSetUserName.setOnClickListener {
            val user = HashMap<String, Any>()
            user["email"] = FirebaseAuth.getInstance().currentUser?.email.toString()
            user["name"] = etUsername.text.toString()


            db?.collection("users")?.document(FirebaseAuth.getInstance().currentUser?.email.toString())

                    //?.add(user)
                    ?.update(user)
                    ?.addOnSuccessListener { documentReference ->
                        Log.d("TAG_PROFILE_SETTINGS", "Added")
                        val navigationView: NavigationView? = activity?.findViewById(R.id.nav_view)
                        val headerview: View? = navigationView?.getHeaderView(0)
                        val tvUserName: TextView? = headerview?.findViewById(R.id.tvUserName)
                        tvUserName?.text = etUsername.text.toString()
                    }
                    ?.addOnFailureListener { e ->
                        Log.w("TAG_PROFILE_SETTINGS", "Error adding document", e)
                    }

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_settings, container, false)
    }




}
