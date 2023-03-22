package com.jose.fitnessgo.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.jose.fitnessgo.data.firebase.FirebaseAuthHelper
import com.jose.fitnessgo.data.firebase.FirestoreHelper
import java.util.Locale

class MapsViewModel : ViewModel() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val newTargetPointPenalty = 100
    var userPointsLiveData = MutableLiveData<Int>()
    var userPoints = 0
    var startTimeOfRound = System.currentTimeMillis()
    var distanceInMeters: Int = 1000000
    var targetAddress: String = ""
    lateinit var context: Context

    var userLocation: Location = Location(LocationManager.GPS_PROVIDER)
    val targetLocation = Location(LocationManager.GPS_PROVIDER)

    var proxIntent: Intent? = null
    var proxPendIntent: PendingIntent? = null

    fun isUserCloseEnough(): Boolean {
        return userLocation.distanceTo(targetLocation) < EXPECTED_RANGE_TO_TARGET
    }

    fun calculateNewPoints(currentTime: Long, prevTime: Long, distanceInMeters: Int): Int {

        val timeTaken = currentTime - prevTime

        var earnedPoints = (distanceInMeters * 5 - timeTaken / 1000).toDouble()
        if (earnedPoints < 10) {
            earnedPoints = 0.0
        }

        //Disabling the Claim Points button, because the points were already given to the user

        return earnedPoints.toInt()
    }

    /**
     * Requests the last known location of the device
     * @param oclCallback
     */
    fun getLastKnownLocation(oclCallback: OnCompleteListener<Location>) {
        Log.d("MapsViewModel", "getLastKnownLocation: called.")

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(oclCallback)
    }

    /**
     * Loads the user points from the Firestore cloud database
     * updates the points in the userPoints variable of this activity
     */
    fun loadPtsFromDb() {
        FirebaseAuthHelper.currentUser()?.email.toString().let {
            FirestoreHelper.loadUserData(
                it,
                { userProfile ->
                    userPoints = userProfile.points
                    userPointsLiveData.postValue(userPoints)
                },
                {})
        }
    }

    /**
     * Updates the "points" value in the user's Firestore document with the current points
     */
    fun savePtsToDb(points: Int) {
        FirebaseAuthHelper.currentUser()?.email?.let {
            FirestoreHelper.saveUserData(it, points)
        }
    }

    fun gameRoundFinished(): Int {
        val newPoints = calculateNewPoints(System.currentTimeMillis(), startTimeOfRound, distanceInMeters)
        userPoints += newPoints
        savePtsToDb(userPoints)
        userPointsLiveData.postValue(userPoints)
        return newPoints
    }

    fun newTargetLocation(): LatLng? {
        var newTargetLatLng = LatLng(
            userLocation.latitude - 0.0025 + Math.random() * 0.005,
            userLocation.longitude - 0.0025 * 1.48 + Math.random() * 0.005 * 1.48
        )

        val geocoder = Geocoder(context, Locale.getDefault())

        // Geocoding and inverse geocoding to get the nearest route to the shuffled coordinates
        try {
            val addresses = geocoder.getFromLocation(newTargetLatLng.latitude, newTargetLatLng.longitude, 1)
            targetAddress = addresses!![0].getAddressLine(0)

            newTargetLatLng = LatLng(addresses[0].latitude, addresses[0].longitude)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Measuring the starting distance to the target
        targetLocation.latitude = newTargetLatLng.latitude
        targetLocation.longitude = newTargetLatLng.longitude
        distanceInMeters = userLocation.distanceTo(targetLocation).toInt()


        startTimeOfRound = System.currentTimeMillis()

        // Building the Proximity Alert
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        proxIntent = Intent("com.jose.fitnessgo.ProximityAlert")
        proxPendIntent = PendingIntent
            .getBroadcast(
                context, 0, proxIntent!!,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        lm.removeProximityAlert(proxPendIntent!!)
        lm.addProximityAlert(
            newTargetLatLng.latitude,
            newTargetLatLng.longitude,
            100f,
            -1,
            proxPendIntent!!
        )


        return newTargetLatLng
    }

    companion object {
        const val EXPECTED_RANGE_TO_TARGET = 100
    }

}
