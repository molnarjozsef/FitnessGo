package com.jose.fitnessgo.ui

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.fragment_profile_settings.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ProfileSettingsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ProfileSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ProfileSettingsFragment : Fragment() {


    private var db: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mAuth = FirebaseAuth.getInstance()
        tvUserEmail.text = mAuth.currentUser?.email.toString()

        btnSetUserName.setOnClickListener {
            val user = HashMap<String, Any>()
            user["email"] = FirebaseAuth.getInstance().currentUser?.email.toString()
            user["name"] = etUsername.text.toString()


            db?.collection("users")?.document(FirebaseAuth.getInstance().currentUser?.email.toString())

                    //?.add(user)
                    ?.update(user)
                    ?.addOnSuccessListener { documentReference ->
                        Log.d("TAG_PROFILE_SETTINGS", "Added")
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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

}
