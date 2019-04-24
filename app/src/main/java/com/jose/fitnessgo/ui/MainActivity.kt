package com.jose.fitnessgo.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
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
import com.jose.fitnessgo.R.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    // Firebase, Firestore
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        val toolbar = findViewById<Toolbar>(id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                string.navigation_drawer_open, string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(id.fragment_container,
                    HomeFragment()).commit()
            //navigationView.setCheckedItem(com.jose.fitnessgo.R.id.nav_message)
        }


        // Firebase, firestore instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Showing email in the drawer header
        val navigationView: NavigationView = findViewById(id.nav_view)
        val headerview: View = navigationView.getHeaderView(0)
        val profileEmail: TextView = headerview.findViewById(id.tvUserEmail)
        val profileUserName: TextView = headerview.findViewById(id.tvUserName)
        profileEmail.text = auth.currentUser?.email.toString()



        navigationView.setNavigationItemSelectedListener {
            if (it.itemId == id.nav_logout) {
                auth.signOut()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
            return@setNavigationItemSelectedListener true
        }


        // Showing username in the drawer header if available
        // If user profile is not found in the DB, user is added to the DB

        var docRef = db.collection("users").document(FirebaseAuth.getInstance().currentUser?.email.toString())
        docRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        profileUserName.text = (document.get("name") ?: " ").toString()
                    } else {
                        val user = HashMap<String, Any>()
                        user["email"] = FirebaseAuth.getInstance().currentUser?.email.toString()
                        db.collection("users").document(FirebaseAuth.getInstance().currentUser?.email.toString()).set(user)
                    }
                }
                .addOnFailureListener { exception ->
                    Snackbar.make(drawer_layout, "Getting user data failed.", Snackbar.LENGTH_LONG).show()
                }
    }

    override fun onResume() {
        isServicesOK
        isMapsEnabled
        checkRequestLocationPermission()

        super.onResume()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    /**
     * Checks for Google Play services on the current device
     * @return true if services are OK
     */
    // Everything is fine and the user can make map requests
    // An error occurred but we can resolve it
    private val isServicesOK: Boolean
        get() {
            Log.d(TAG, "isServicesOK: checking google services version")

            val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)

            when {
                available == ConnectionResult.SUCCESS -> {
                    Log.d(TAG, "isServicesOK: Google Play Services is working")
                    return true
                }
                GoogleApiAvailability.getInstance().isUserResolvableError(available) -> {
                    Log.d(TAG, "isServicesOK: an error occured but we can fix it")
                    val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this@MainActivity, available, ERROR_DIALOG_REQUEST)
                    dialog.show()
                }
                else -> Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
            }
            return false
        }


    /**
     * Checks if GPS location is enabled on the current device
     * @return true if enabled
     */
    private val isMapsEnabled: Boolean
        get() {
            val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
                return false
            }
            return true
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
        builder.setMessage("This application requires active GPS to work properly.")
                .setCancelable(false)
                .setPositiveButton("Open GPS settings") { _, _ ->
                    val enableGpsIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(enableGpsIntent)
                }
        val alert = builder.create()
        alert.show()
    }

    companion object {

        private const val TAG = "MainActivity"
    }

}
