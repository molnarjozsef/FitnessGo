package com.jose.fitnessgo.ui

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var targetLatLng: LatLng? = null
    private var userLocation: Location = Location(LocationManager.GPS_PROVIDER)
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var distanceInMeters: Int = 1000000
    private var startTimeOfRound = System.currentTimeMillis()
    private var pxr: AlertOnProximityReceiver? = null
    private var proxIntent: Intent? = null
    private var proxPendIntent: PendingIntent? = null
    private val targetLocation = Location(LocationManager.GPS_PROVIDER)
    private var userPoints: Int = 0


    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        db = FirebaseFirestore.getInstance()

        loadPtsFromDb()

        getLastKnownLocation(oclNewTarget)

        btnNewTarget.setOnClickListener{
            getLastKnownLocation(oclNewTarget)
        }

        btnClaimPoints.setOnClickListener{
            getLastKnownLocation(oclClaimPoints)
        }

        btnSavePts?.setOnClickListener{
            savePtsToDb()
        }

        btnLoadPts?.setOnClickListener{
            loadPtsFromDb()
        }


        val filter = IntentFilter("com.jose.fitnessgo.ProximityAlert")
        pxr = AlertOnProximityReceiver()
        registerReceiver(pxr, filter)
    }

    override fun onResume() {
        loadPtsFromDb()
        super.onResume()
    }

    override fun onPause() {
        savePtsToDb()
        super.onPause()
    }

    /**
     * Updates the TextView that shows the user points from the Integer parameter
     */
    fun refreshUserPointsView(pts: Int){
        tvUserPoints.text = resources.getString(R.string._points,pts)
    }


    /**
     * Loads the user points from the Firestore cloud database
     * updates the points in the userPoints variable of this activity
     */
    private fun loadPtsFromDb(){
        db.collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.email.toString())
                .get()
                .addOnSuccessListener { document ->
                    if (document?.get("points") != null) {
                        userPoints = Integer.parseInt(document.get("points").toString())
                        refreshUserPointsView(userPoints)
                    } else {
                        userPoints = 0
                        refreshUserPointsView(userPoints)
                        savePtsToDb()
                    }

                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
    }


    /**
     * Updates the "points" value in the user's Firestore document with the current points
     */
    fun savePtsToDb(){

        val userPtsData = HashMap<String, Any>()
        userPtsData["email"] = FirebaseAuth.getInstance().currentUser?.email.toString()
        userPtsData["points"] = userPoints

        db.collection("users").document(FirebaseAuth.getInstance().currentUser?.email.toString())

                //?.add(user)
                .update(userPtsData)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        savePtsToDb()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap!!.isMyLocationEnabled = true
    }

    /**
     * Requests the last known location of the device
     * @param ocl
     */
    private fun getLastKnownLocation(ocl: OnCompleteListener<Location>) {
        Log.d(TAG, "getLastKnownLocation: called.")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationProviderClient!!.lastLocation.addOnCompleteListener(ocl)
    }

    private fun newTargetLocation() {
        targetLatLng = LatLng(userLocation.latitude - 0.0025 + Math.random() * 0.005,
                userLocation.longitude - 0.0025 * 1.48 + Math.random() * 0.005 * 1.48)


        val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(targetLatLng!!.latitude, targetLatLng!!.longitude, 1)
            tvTargetAddress.text = addresses[0].getAddressLine(0)

            targetLatLng = LatLng(addresses[0].latitude, addresses[0].longitude)

        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Measuring the starting distance to the target
        targetLocation.latitude = targetLatLng!!.latitude
        targetLocation.longitude = targetLatLng!!.longitude
        distanceInMeters = userLocation.distanceTo(targetLocation).toInt()
        tvTargetAddress.append("\n" + getString(R.string.this_round_is) + " "
                + distanceInMeters.toString() + " " + getString(R.string.meters))

        startTimeOfRound = System.currentTimeMillis()

        // Building the Proximity Alert
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        proxIntent = Intent("com.jose.fitnessgo.ProximityAlert")
        proxPendIntent = PendingIntent
                .getBroadcast(applicationContext, 0, proxIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        lm.removeProximityAlert(proxPendIntent)
        lm.addProximityAlert(
                targetLatLng!!.latitude,
                targetLatLng!!.longitude,
                100f,
                -1,
                proxPendIntent)


        // Updating the map with the new location
        // And refocusing the view between the user and the target
        mMap!!.clear()
        mMap!!.addMarker(MarkerOptions().position(targetLatLng!!)
                .title("This is where you should get, as fast as you can"))
        mMap!!.animateCamera(CameraUpdateFactory
                .newLatLngZoom(LatLng(
                        (targetLocation.latitude + userLocation.latitude) / 2,
                        (targetLocation.longitude + userLocation.longitude) / 2),
                        16.0f))


        // Re-enabling the Claim Points button, because the new location's points were not yet claimed
        btnClaimPoints?.isEnabled = true
    }


    /**
     * This function should be called when the user gets to the target location
     * It calculates the earned points, and gives it back
     */
    internal fun calculateNewPoints(userPts: Int, currentTime: Long, prevTime: Long): Int {

        val timeTaken = currentTime - prevTime


        var earnedPoints = (distanceInMeters * 5 - timeTaken / 1000).toDouble()
        if (earnedPoints < 10) {
            earnedPoints = 0.0
        }

        //Disabling the Claim Points button, because the points were already given to the user
        btnClaimPoints?.isEnabled = false
        return (userPts + earnedPoints).toInt()
    }

    /**
     * OnCompleteListener for the getLastKnownLocation function,
     * After the location request completion,
     * checks if the location claim was real, calculates the new points
     * and updates the textview containing the points.
     */
    private var oclClaimPoints: OnCompleteListener<Location> = OnCompleteListener { task ->
        if (task.isSuccessful) {
            userLocation = task.result ?: userLocation
        }
        if (userLocation.distanceTo(targetLocation) < EXPECTED_RANGE_TO_TARGET) {
            userPoints = calculateNewPoints(userPoints, System.currentTimeMillis(), startTimeOfRound)
            refreshUserPointsView(userPoints)
            savePtsToDb()
        } else {
            Toast.makeText(this@MapsActivity,
                    getString(R.string.not_close_enough) + "\n" +
                            userLocation.distanceTo(targetLocation).toInt() + " " +
                            getString(R.string._meters_still_to_go),
                    Toast.LENGTH_LONG).show()
        }
    }

    /**
     * OnCompleteListener for the getLastKnownLocation function
     * After the location request completion, generates a new position
     * and updates the map and the textviews.
     */
    private var oclNewTarget: OnCompleteListener<Location> = OnCompleteListener { task ->
        if (task.isSuccessful) {
            userLocation = task.result ?: userLocation
        }
        newTargetLocation()
    }



    /**
     * A BroadcastReceiver for the Proximity alert PendingIntent
     */
    inner class AlertOnProximityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            tvTargetAddress.append("\n\n Destination reached!")
            userPoints = calculateNewPoints(userPoints, System.currentTimeMillis(), startTimeOfRound)
            refreshUserPointsView(userPoints)
            savePtsToDb()
        }
    }

    companion object {

        private const val TAG = "MAPS_ACTIVITY"
        const val EXPECTED_RANGE_TO_TARGET = 100
    }
}
