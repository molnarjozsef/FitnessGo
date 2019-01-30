package com.jose.fitnessgo.ui;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.jose.fitnessgo.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    public static final int EXPECTED_RANGE_TO_TARGET = 100;

    private GoogleMap mMap;
    private LatLng targetLatLng;
    private Location userLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private TextView tvTargetAddress;
    private TextView tvUserPoints;
    private Integer distanceInMeters;
    private long startTime = System.currentTimeMillis();
    private AlertOnProximityReceiver pxr;
    private Intent intent;
    private PendingIntent proxIntent;
    private Location targetLocation = new Location(LocationManager.GPS_PROVIDER);
    Button btnClaimPoints;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tvTargetAddress = findViewById(R.id.tvTargetAddress);
        tvUserPoints = findViewById(R.id.tvUserPoints);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getLastKnownLocation(oclNewTarget);

        Button btnNewTarget = findViewById(R.id.btnNewTarget);
        btnNewTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastKnownLocation(oclNewTarget);
            }
        });

        btnClaimPoints = findViewById(R.id.btnClaimPoints);
        btnClaimPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastKnownLocation(oclClaimPoints);
            }
        });


        IntentFilter filter = new IntentFilter("com.jose.fitnessgo.ProximityAlert");
        pxr = new AlertOnProximityReceiver();
        registerReceiver(pxr, filter);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Requests the last known location of the device
     * @param ocl
     */
    public void getLastKnownLocation(@NonNull OnCompleteListener<Location> ocl) {
        Log.d(TAG, "getLastKnownLocation: called.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(ocl);
    }

    /**
     * OnCompleteListener for the getLastKnownLocation function,
     * After the location request completion,
     * checks if the location claim was real, calculates the new points
     * and updates the textview containing the points.
     */
    OnCompleteListener<Location> oclClaimPoints = new OnCompleteListener<Location>() {
        @Override
        public void onComplete(@NonNull Task<Location> task) {
            if (task.isSuccessful()) {
                userLocation = task.getResult();
            }
            if(userLocation.distanceTo(targetLocation) < EXPECTED_RANGE_TO_TARGET){
                //Todo: fails at midnight
                long timeTaken = System.currentTimeMillis()-startTime;
                double userPoints = (float) Integer.parseInt(tvUserPoints.getText()
                        .toString().replaceAll("[\\D]", ""));
                double earnedPoints = (distanceInMeters*1000/(Math.sqrt(timeTaken)));
                int newPoints = (int)(userPoints + earnedPoints);
                tvUserPoints.setText(newPoints + " points");
                //getLastKnownLocation(true);
                btnClaimPoints.setEnabled(false);
            }else{
                Toast.makeText(MapsActivity.this,"Not close enough.",Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * OnCompleteListener for the getLastKnownLocation function
     * After the location request completion, generates a new position
     * and updates the map and the textviews.
     */
    OnCompleteListener<Location> oclNewTarget = new OnCompleteListener<Location>() {
        @Override
        public void onComplete(@NonNull Task<Location> task) {
            if (task.isSuccessful()) {
                userLocation = task.getResult();
            }
            newTargetLocation();
        }
    };

    public void newTargetLocation() {
        targetLatLng = new LatLng(userLocation.getLatitude() - 0.0025 + Math.random() * 0.005,
                userLocation.getLongitude() - 0.0025 * 1.48 + Math.random() * 0.005 * 1.48);


        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.
                    getFromLocation(targetLatLng.latitude, targetLatLng.longitude, 1);
            tvTargetAddress.setText(addresses.get(0).getAddressLine(0).toString());

            targetLatLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add a marker to the generated target point and move the camera
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(targetLatLng)
                .title("This is where you should get, as fast as you can"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(targetLatLng));

        // Measuring the starting distance to the target
        targetLocation.setLatitude(targetLatLng.latitude);
        targetLocation.setLongitude(targetLatLng.longitude);
        distanceInMeters = (int) userLocation.distanceTo(targetLocation);
        tvTargetAddress.append("\n" + distanceInMeters.toString() + " meters away");

        startTime = System.currentTimeMillis();

        // Proximity Alert
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        intent = new Intent("com.jose.fitnessgo.ProximityAlert");
        proxIntent = PendingIntent
                .getBroadcast(getApplicationContext(), 0, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.removeProximityAlert(proxIntent);
        lm.addProximityAlert(targetLatLng.latitude, targetLatLng.longitude, 100f, -1, proxIntent);



        mMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(
                        (targetLocation.getLatitude() + userLocation.getLatitude())/2,
                        (targetLocation.getLongitude() + userLocation.getLongitude())/2),
                        16.0f));


        btnClaimPoints.setEnabled(true);
    }


    /**
     * A BroadcastReceiver for the Proximity alert PendingIntent
     */
    public class AlertOnProximityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {

            tvTargetAddress.append("\n\n Destination reached!");
            long timeTaken = System.currentTimeMillis()-startTime;
            double userPoints = (float) Integer.parseInt(tvUserPoints.getText()
                    .toString().replaceAll("[\\D]", ""));
            double earnedPoints = (distanceInMeters*1000/(Math.sqrt(timeTaken)));
            int newPoints = (int)(userPoints + earnedPoints);
            tvUserPoints.setText(newPoints + " points");
            //getLastKnownLocation(true);
            btnClaimPoints.setEnabled(false);
        }
    }
}
