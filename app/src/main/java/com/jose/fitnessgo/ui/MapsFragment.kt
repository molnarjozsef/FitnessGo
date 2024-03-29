package com.jose.fitnessgo.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.jose.fitnessgo.R
import kotlinx.android.synthetic.main.fragment_maps.*

class MapsFragment : Fragment() {


    private var mMap: GoogleMap? = null
    private var pxr: AlertOnProximityReceiver? = null
    private val filter = IntentFilter("com.jose.fitnessgo.ProximityAlert")

    lateinit var viewModel: MapsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
        viewModel.context = this.context!!

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

        val userPointsObserver = Observer<Int>{userPoints ->
            refreshUserPointsView(userPoints)
        }

        viewModel.userPointsLiveData.observe(this,userPointsObserver)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { onMapReady(it) }

        viewModel.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.activity!!)

        viewModel.loadPtsFromDb()
        refreshUserPointsView(viewModel.userPoints)

        viewModel.getLastKnownLocation(oclNewTarget)


        btnNewTarget.setOnClickListener {
            when (viewModel.userPoints) {
                in 0..viewModel.newTargetPointPenalty -> {
                    Snackbar.make(
                            clLayoutMapsFragment,
                            "Not enough points. You need to have ${viewModel.newTargetPointPenalty
                                    - viewModel.userPoints} more points to get a new target.",
                            Snackbar.LENGTH_LONG
                    ).show()
                }
                else -> {
                    viewModel.userPoints -= viewModel.newTargetPointPenalty
                    refreshUserPointsView(viewModel.userPoints)
                    viewModel.savePtsToDb(viewModel.userPoints)
                    viewModel.getLastKnownLocation(oclNewTarget)
                }

            }
        }

        btnNextRound?.setOnClickListener {
            viewModel.getLastKnownLocation(oclNewTarget)
        }

        btnClaimPoints.setOnClickListener {
            viewModel.getLastKnownLocation(oclClaimPoints)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.userPointsLiveData.removeObservers(this)
    }

    override fun onResume() {


        viewModel.loadPtsFromDb()
        refreshUserPointsView(viewModel.userPoints)
        super.onResume()
    }

    override fun onPause() {
        viewModel.savePtsToDb(viewModel.userPoints)
        super.onPause()
    }

    /**
     * Updates the TextView that shows the user points from the Integer parameter
     */
    private fun refreshUserPointsView(pts: Int) {
        tvUserPoints.text = resources.getString(R.string._points, pts)
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
    private fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this.context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(this.context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap!!.isMyLocationEnabled = true

    }


    private fun updateMap(targetLatLng: LatLng) {
        // Updating the map with the new location
        // And refocusing the view between the user and the target
        mMap!!.clear()
        mMap!!.addMarker(MarkerOptions().position(targetLatLng)
                .title("This is where you should get, as fast as you can"))
        mMap!!.animateCamera(CameraUpdateFactory
                .newLatLngZoom(LatLng(
                        (targetLatLng.latitude + viewModel.userLocation.latitude) / 2,
                        (targetLatLng.longitude + viewModel.userLocation.longitude) / 2),
                        16.0f))

    }


    /**
     * This function should be called when the user gets to the target location
     * It calculates the earned points, and gives it back
     */


    private fun setLocationButtonsEnabled(isEnabled: Boolean) {
        btnClaimPoints.isEnabled = isEnabled
        btnNewTarget.isEnabled = isEnabled
    }

    /**
     * OnCompleteListener for the getLastKnownLocation function,
     * After the location request completion,
     * checks if the location claim was real, calculates the new points
     * and updates the textview containing the points.
     */
    private var oclClaimPoints: OnCompleteListener<Location> = OnCompleteListener { task ->
        if (task.isSuccessful) {
            viewModel.userLocation = task.result ?: viewModel.userLocation
        }
        if (viewModel.isUserCloseEnough()) {
            val lm = this.context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.removeProximityAlert(viewModel.proxPendIntent)
            roundFinished()

        } else {
            Snackbar.make(
                    clLayoutMapsFragment,
                    getString(R.string.not_close_enough) + " " +
                            viewModel.userLocation.distanceTo(viewModel.targetLocation).toInt() + " " +
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
            viewModel.userLocation = task.result ?: viewModel.userLocation
        }

        val newTarget = viewModel.newTargetLocation()
        tvTargetAddress.text = viewModel.targetAddress + "\n" + resources.getString(R.string.this_round_is) + " " + viewModel.distanceInMeters.toString() + " " + resources.getString(R.string.meters)

        newTarget?.let { updateMap(newTarget) }

        // Re-enabling the Claim Points button, because the new location's points were not yet claimed
        setLocationButtonsEnabled(true)
        // Making the nextround button gone
        btnNextRound?.visibility = View.GONE
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

        val newPoints = viewModel.gameRoundFinished()

        setLocationButtonsEnabled(false)
        tvUserPoints?.let { refreshUserPointsView(viewModel.userPoints) }
        showNewPointsSnackbar(newPoints)
        btnNextRound?.visibility = View.VISIBLE
        tvTargetAddress?.append("\n\n Destination reached!")
    }

    companion object {

        private const val TAG = "MAPS_ACTIVITY"

    }

}
