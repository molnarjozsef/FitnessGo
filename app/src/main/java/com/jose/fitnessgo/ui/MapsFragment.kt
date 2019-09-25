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
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jose.fitnessgo.R
import com.jose.fitnessgo.data.firebase.FirebaseAuthHelper
import com.jose.fitnessgo.data.firebase.FirestoreHelper
import kotlinx.android.synthetic.main.fragment_maps.*
import java.util.*

class MapsFragment : Fragment() {


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
    private val filter = IntentFilter("com.jose.fitnessgo.ProximityAlert")
    private val newTargetPointPenalty = 100


    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pxr = AlertOnProximityReceiver()
        this.activity?.registerReceiver(pxr, filter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { onMapReady(it) }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
        db = FirebaseFirestore.getInstance()

        loadPtsFromDb()

        getLastKnownLocation(oclNewTarget)





        btnNewTarget.setOnClickListener {
            when (userPoints) {
                in 0..newTargetPointPenalty -> {
                    Snackbar.make(
                            clLayoutMapsFragment,
                            "Not enough points. You need to have ${newTargetPointPenalty
                                    - userPoints} more points to get a new target.",
                            Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {
                    userPoints -= newTargetPointPenalty
                    refreshUserPointsView(userPoints)
                    savePtsToDb()
                    getLastKnownLocation(oclNewTarget)
                }

            }
        }

        btnNextRound?.setOnClickListener {
            getLastKnownLocation(oclNewTarget)
        }

        btnClaimPoints.setOnClickListener {
            getLastKnownLocation(oclClaimPoints)
        }

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
    fun refreshUserPointsView(pts: Int) {
        tvUserPoints.text = resources.getString(R.string._points, pts)
    }


    /**
     * Loads the user points from the Firestore cloud database
     * updates the points in the userPoints variable of this activity
     */
    private fun loadPtsFromDb() {
        FirebaseAuthHelper.currentUser()?.email.toString().let {
            FirestoreHelper.loadUserData(
                    it,
                    { userProfile ->
                        userPoints = userProfile.points
                        refreshUserPointsView(userPoints)
                    },
                    {})
        }
    }


    /**
     * Updates the "points" value in the user's Firestore document with the current points
     */
    fun savePtsToDb() {
        FirebaseAuthHelper.currentUser()?.email?.let {
            FirestoreHelper.saveUserData(it, userPoints)
        }
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
    fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        if (ActivityCompat.checkSelfPermission(this.context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(this.context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap!!.isMyLocationEnabled = true

    }

    /**
     * Requests the last known location of the device
     * @param oclCallback
     */
    private fun getLastKnownLocation(oclCallback: OnCompleteListener<Location>) {
        Log.d(TAG, "getLastKnownLocation: called.")

        if (ActivityCompat.checkSelfPermission(this.context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(this.context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationProviderClient.lastLocation.addOnCompleteListener(oclCallback)
    }

    private fun newTargetLocation() {
        targetLatLng = LatLng(userLocation.latitude - 0.0025 + Math.random() * 0.005,
                userLocation.longitude - 0.0025 * 1.48 + Math.random() * 0.005 * 1.48)

        val geocoder = Geocoder(this.activity, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(targetLatLng!!.latitude, targetLatLng!!.longitude, 1)
            tvTargetAddress.text = addresses[0].getAddressLine(0)

            targetLatLng = LatLng(addresses[0].latitude, addresses[0].longitude)

        } catch (e: Exception) {
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
        val lm = this.context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        proxIntent = Intent("com.jose.fitnessgo.ProximityAlert")
        proxPendIntent = PendingIntent
                .getBroadcast(this.context, 0, proxIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT)
        if (ActivityCompat.checkSelfPermission(this.context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(this.context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        btnClaimPoints.isEnabled = true
        btnNewTarget.isEnabled = true
        // Making the nextround button gone
        btnNextRound?.visibility = View.GONE
    }


    /**
     * This function should be called when the user gets to the target location
     * It calculates the earned points, and gives it back
     */
    internal fun calculateNewPoints(currentTime: Long, prevTime: Long): Int {

        val timeTaken = currentTime - prevTime


        var earnedPoints = (distanceInMeters * 5 - timeTaken / 1000).toDouble()
        if (earnedPoints < 10) {
            earnedPoints = 0.0
        }

        //Disabling the Claim Points button, because the points were already given to the user
        btnClaimPoints.isEnabled = false
        btnNewTarget.isEnabled = false
        return earnedPoints.toInt()
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
            val lm = this.context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.removeProximityAlert(proxPendIntent)


        } else {
            Snackbar.make(
                    clLayoutMapsFragment,
                    getString(R.string.not_close_enough) + " " +
                            userLocation.distanceTo(targetLocation).toInt() + " " +
                            getString(R.string._meters_still_to_go),
                    Snackbar.LENGTH_LONG).show()
        }

    }

    private fun showNewPointsSnackbar(newPoints: Int) {
        when (newPoints) {
            0 -> {
                Snackbar.make(
                        clLayoutMapsFragment,
                        getString(R.string.congratulations_zero_points),
                        Snackbar.LENGTH_LONG).show()
            }
            1 -> {
                Snackbar.make(
                        clLayoutMapsFragment,
                        getString(R.string.congratulations_one_point),
                        Snackbar.LENGTH_LONG).show()
            }
            else -> {
                Snackbar.make(
                        clLayoutMapsFragment,
                        getString(R.string.congratulations_you_scored__points, newPoints),
                        Snackbar.LENGTH_LONG).show()
            }
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
            roundFinished()
        }
    }

    fun roundFinished() {
        tvTargetAddress?.append("\n\n Destination reached!")
        val newPoints = calculateNewPoints(System.currentTimeMillis(), startTimeOfRound)
        userPoints += newPoints
        tvUserPoints?.let { refreshUserPointsView(userPoints) }
        savePtsToDb()
        showNewPointsSnackbar(newPoints)
        btnNextRound?.visibility = View.VISIBLE

    }

    companion object {

        private const val TAG = "MAPS_ACTIVITY"
        const val EXPECTED_RANGE_TO_TARGET = 100
    }

}
