package com.jose.fitnessgo.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.jose.fitnessgo.Constants.ERROR_DIALOG_REQUEST
import com.jose.fitnessgo.Constants.PERMISSIONS_REQUEST_FINE_LOCATION
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.activity_home.*

class MainActivity : AppCompatActivity() {


    // Variables
    private val mLocationPermissionGranted = false


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        bankcardId.setOnClickListener {
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            startActivity(intent)
        }

        /*
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager

        val adapter = MenuItemAdapter(this, Supplier.menuItems)
        recyclerView.adapter = adapter
        */


    }

    override fun onResume() {
        super.onResume()

        isServicesOK
        isMapsEnabled
        checkRequestLocationPermission()
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
