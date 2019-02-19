package com.jose.fitnessgo.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jose.fitnessgo.Constants.ERROR_DIALOG_REQUEST
import com.jose.fitnessgo.Constants.PERMISSIONS_REQUEST_FINE_LOCATION
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    // Variables
    private val mLocationPermissionGranted = false
    private var db: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.jose.fitnessgo.R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(com.jose.fitnessgo.R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                com.jose.fitnessgo.R.string.navigation_drawer_open, com.jose.fitnessgo.R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(com.jose.fitnessgo.R.id.fragment_container,
                    HomeFragment()).commit()
            //navigationView.setCheckedItem(com.jose.fitnessgo.R.id.nav_message)
        }

        val mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val navigationView: NavigationView = findViewById(R.id.nav_view);
        val headerview: View = navigationView.getHeaderView(0);
        val profileEmail: TextView = headerview.findViewById(R.id.tvUserEmail);
        profileEmail.text = mAuth.currentUser?.email.toString()

        db?.collection("users")
                ?.get()
                ?.addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d("TAG_MAIN_ACTIVITY", document.id + " => " + document.data)
                        if(document.get("email") == FirebaseAuth.getInstance().currentUser?.email.toString()){
                            profileEmail.text = (document.get("name") ?: mAuth.currentUser?.email).toString()

                        }
                    }
                }
                ?.addOnFailureListener { exception ->
                    Log.w("TAG_MAIN_ACTIVITY", "Error getting documents.", exception)
                }


    }

    override fun onBackPressed(){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }


    /**
     * Checks for Google Play services on the current device
     * @return
     */
    // Everything is fine and the user can make map requests
    // An error occured but we can resolve it
    val isServicesOK: Boolean
        get() {
            Log.d(TAG, "isServicesOK: checking google services version")

            val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)

            if (available == ConnectionResult.SUCCESS) {
                Log.d(TAG, "isServicesOK: Google Play Services is working")
                return true
            } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
                Log.d(TAG, "isServicesOK: an error occured but we can fix it")
                val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this@MainActivity, available, ERROR_DIALOG_REQUEST)
                dialog.show()
            } else {
                Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
            }
            return false
        }


    /**
     * Checks if GPS location is enabled on the current device
     * @return
     */
    val isMapsEnabled: Boolean
        get() {
            val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
                return false
            }
            return true
        }



    override fun onResume() {

        isServicesOK
        isMapsEnabled
        checkRequestLocationPermission()




        super.onResume()
    }



    /**
     * Checks for fine location access permission, and requests if not yet granted
     */
    private fun checkRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_FINE_LOCATION)
        }
    }

    /**
     * Opens an alert message that tells the user to enable GPS,
     * and opens the GPS enabling system settings menu
     */
    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This application requires GPS to work at all, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    val enableGpsIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(enableGpsIntent)
                }
        val alert = builder.create()
        alert.show()
    }

    companion object {

        private val TAG = "MainActivity"
    }

}
