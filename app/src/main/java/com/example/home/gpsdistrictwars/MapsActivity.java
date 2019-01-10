package com.example.home.gpsdistrictwars;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.xml.transform.dom.DOMLocator;

public class MapsActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "!MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;
    private LatLng mLatLng, sLatLng;
    private static DecimalFormat df3 = new DecimalFormat("#.###");
    private Button bCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.d(TAG, "onCreate: Activity has been created");

        getLocationPermissions();

    }

    private void setUpActivityDetails(){
        Log.d(TAG, "setUpActivityDetails: Setting up activity details");
        bCapture = (Button) findViewById(R.id.bCapture);
        bCapture.setOnClickListener(this);
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            mLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            moveCamera( mLatLng, DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable tp get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng mLatLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to Lat: " + mLatLng.latitude + ", Lng: " + mLatLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, zoom));
        drawMap();
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermissions() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);

        }
    }

    private void drawMap(){
        Log.d(TAG, "drawMap: drawing map started");
        drawingCurrentLocation(fixingLatLng(mLatLng));

        setUpActivityDetails();
    }

    private void drawingCurrentLocation(LatLng cLatLng){
        Polygon mPolygon = mMap.addPolygon(new PolygonOptions().add(new LatLng(cLatLng.latitude, cLatLng.longitude),
                new LatLng(cLatLng.latitude, cLatLng.longitude + 0.001),
                new LatLng(cLatLng.latitude - 0.001, cLatLng.longitude + 0.001),
                new LatLng(cLatLng.latitude - 0.001, cLatLng.longitude),
                new LatLng(cLatLng.latitude, cLatLng.longitude)).strokeWidth(10).strokeColor(Color.YELLOW)
        );
        Log.d(TAG, "drawingCurrentLocation: Has been drawn");
    }

    private LatLng fixingLatLng(LatLng mLatLng){
        df3.setRoundingMode(RoundingMode.UP);
        sLatLng = new LatLng(Double.parseDouble(df3.format(mLatLng.latitude)), Double.parseDouble(df3.format(mLatLng.longitude)));
        Log.d(TAG, "fixingLatLng: rounded LatLng is " + sLatLng.toString());
        return sLatLng;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bCapture:
                break;
        }
    }
}
