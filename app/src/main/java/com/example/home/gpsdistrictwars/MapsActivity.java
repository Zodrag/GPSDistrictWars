package com.example.home.gpsdistrictwars;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback, android.location.LocationListener {

    private GoogleMap mMap;
    private static final String TAG = "!MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int PERMISSION_LOCATION_REQUEST = 6547;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;
    private LatLng mLatLng;
    private static DecimalFormat df3u = new DecimalFormat("#.###");
    private static DecimalFormat df3d = new DecimalFormat("#.###");
    private Button bCapture;
    private static final LatLng mDefaultLocation = new LatLng(30, -90.0);
    private Polygon mPolygon, cPolygon;
    private LocationManager locationManager;
    List<LatLng> allLatLng, updatedLatLng, onStartAllLatLng, onUpdateAllLatLng, differenceLatLng;
    List<Polygon> allCapturedPolygons;
    List<List<LatLng>> allDistrictLatLng, updatedDistrictLatLng;
    List<String> districtNamesList;
    Boolean onStartUp = true, myTerritory = false;
    String cityName, color, districtName;
    UserLocalStore userLocalStore;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef, mRefGetTerritories;
    ValueEventListener valueEventListenerGetTerritories;
    List <DistrictInfo> districtInfo;
    List <TerritoryInfo> territoryInfo;



    @Override
    //1
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.d(TAG, "onCreate: Activity has been created");

        getLocationPermissions();
    }
    //2
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
    //3
    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }
    //4 also called when map resumes
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: listeners started");
        requestLocationUpdates();
        if (!(cityName == null)){
            getCapturedTerritory();
        }
    }
    //5
    private void requestLocationUpdates(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setCostAllowed(true);
        criteria.setSpeedRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setMessage("We need your location to proceed")
                        .setTitle("Locations")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        PERMISSION_LOCATION_REQUEST);

                            }
                        }).create().show();
            } else ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_LOCATION_REQUEST);
            return;
        }
        locationManager.requestLocationUpdates(provider, 10000, 25, this);
        Log.d(TAG, "requestLocationUpdates: provider: " + provider);

    }
    //6
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
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    Log.d(TAG, "onMapClick: " + fixingLatLng(latLng).toString());
                }
            });
            mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
                @Override
                public void onPolygonClick(Polygon polygon) {
                    String tag = Objects.requireNonNull(polygon.getTag()).toString();
                    Log.d(TAG, "onPolygonClick: tag is " + tag);
                }
            });

        }
    }
    //7
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            assert currentLocation != null;
                            mLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            try {
                                moveCamera( mLatLng);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "onComplete: current location is null using default location");
                            try {
                                moveCamera(mDefaultLocation);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }
    //8
    private void moveCamera(LatLng mLatLng) throws IOException {
        Log.d(TAG, "moveCamera: moving the camera to Lat: " + mLatLng.latitude + ", Lng: " + mLatLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, MapsActivity.DEFAULT_ZOOM));
        drawMap();
    }
    //9
    private void drawMap() throws IOException {
        Log.d(TAG, "drawMap: drawing map started");
        drawingCurrentLocation(fixingLatLng(mLatLng));
        setUpActivityDetails();
        getCapturedTerritory();
    }
    //10
    private void drawingCurrentLocation(LatLng cLatLng){
        mPolygon = mMap.addPolygon(new PolygonOptions().add(new LatLng(cLatLng.latitude, cLatLng.longitude),
                new LatLng(cLatLng.latitude, cLatLng.longitude + 0.001),
                new LatLng(cLatLng.latitude - 0.001, cLatLng.longitude + 0.001),
                new LatLng(cLatLng.latitude - 0.001, cLatLng.longitude),
                new LatLng(cLatLng.latitude, cLatLng.longitude)).strokeWidth(15).strokeColor(Color.YELLOW)
        );
        Log.d(TAG, "drawingCurrentLocation: Has been drawn  " + cLatLng.toString());
    }
    //11
    private void setUpActivityDetails() throws IOException {
        Log.d(TAG, "setUpActivityDetails: Setting up activity details");

        userLocalStore = new UserLocalStore(this);

        mDatabase = FirebaseDatabase.getInstance();
        bCapture = findViewById(R.id.bCapture);
        bCapture.setOnClickListener(this);

        allLatLng = new ArrayList<>();
        updatedLatLng = new ArrayList<>();
        allCapturedPolygons = new ArrayList<>();
        allDistrictLatLng = new ArrayList<>();
        updatedDistrictLatLng = new ArrayList<>();
        onStartAllLatLng = new ArrayList<>();
        districtInfo = new ArrayList<>();
        districtNamesList = new ArrayList<>();
        territoryInfo = new ArrayList<>();
        getCityName();
        checkIfYourTerritory(fixingLatLng(mLatLng));
    }
    //12
    private void getCityName() throws IOException {
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = gcd.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1);
        if (addresses.size() > 0) {
            cityName = addresses.get(0).getLocality();
            Log.d(TAG, "getCityName: " + cityName);
        }
        else {
            Toast.makeText(this, "Cant find city", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "getCityName: Unknown");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
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

    private void drawCapturedTerritory(LatLng cLatLng, DistrictInfo districtInfo){
        cPolygon = mMap.addPolygon(new PolygonOptions().add(new LatLng(cLatLng.latitude, cLatLng.longitude),
                new LatLng(cLatLng.latitude, cLatLng.longitude + 0.001),
                new LatLng(cLatLng.latitude - 0.001, cLatLng.longitude + 0.001),
                new LatLng(cLatLng.latitude - 0.001, cLatLng.longitude),
                new LatLng(cLatLng.latitude, cLatLng.longitude)).strokeWidth(3).fillColor(Color.parseColor(districtInfo.color)).clickable(true)
        );

        cPolygon.setTag(cLatLng.toString());
        allCapturedPolygons.add(cPolygon);
    }

    private void saveCapturingTerritory(LatLng cLatLng){
        Log.d(TAG, "saveCapturingTerritory: success");
        bCapture.setVisibility(View.INVISIBLE);
        saveNewTerritory(cLatLng);
    }

    private LatLng fixingLatLng(LatLng mLatLng){
        df3u.setRoundingMode(RoundingMode.UP);
        df3d.setRoundingMode(RoundingMode.DOWN);
        return new LatLng(Double.parseDouble(df3u.format(mLatLng.latitude)), Double.parseDouble(df3u.format(mLatLng.longitude)));
    }

    private LatLng convertStringToLatLng(String sLatLng){
        String nLatLng = sLatLng.replaceAll("[^\\d.,-]", "");
        String[] gpsVal = nLatLng.split(",");
        double lat = Double.parseDouble(gpsVal[0]);
        double lng = Double.parseDouble(gpsVal[1]);
        return new LatLng(lat, lng);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bCapture:
                saveCapturingTerritory(fixingLatLng(mLatLng));
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (fixingLatLng(mLatLng).equals(mPolygon.getPoints().get(0))) {
            Log.d(TAG, "onLocationChanged: current location the same");
        } else {
            mPolygon.remove();
            checkIfYourTerritory(fixingLatLng(mLatLng));
            drawingCurrentLocation(fixingLatLng(mLatLng));
            Log.d(TAG, "onLocationChanged: current location changed");
        }
    }

    private void saveNewTerritory(LatLng cLatLng){
        mRef = mDatabase.getReference().child("Territory").child(cityName).child("DistrictName");

        mRef.child(convertStringToFireBaseChild(cLatLng.toString())).setValue(cLatLng.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "onComplete: saved territory");
                } else {
                    Log.d(TAG, "onComplete: failed to save territory");
                }
            }
        });
    }

    private String convertStringToFireBaseChild(String changingString){
        String firstChange = changingString.replaceFirst("/", "?");
        return firstChange.replaceAll("\\.", "?");
    }

    private void checkIfYourTerritory(final LatLng cLatLng){
        mRef = mDatabase.getReference().child("Territory").child(cityName).child(userLocalStore.getUserDistrict());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allLatLng = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    allLatLng.add(convertStringToLatLng(Objects.requireNonNull(ds.getValue()).toString()));
                }
                myTerritory = allLatLng.contains(cLatLng);
                if (myTerritory){
                    bCapture.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "onDataChange: Friendly Territory, Capture button is invisible");
                } else {
                    bCapture.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onDataChange: Enemy Territory, Capture button visible");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDistrictInformation(final String districtName, final LatLng cLatLng) {
            for (int i = 0; i < districtInfo.size(); i++) {
                if (!(districtNamesList.contains(districtInfo.get(i).districtName))){
                    districtNamesList.add(districtInfo.get(i).districtName);

                }
            }
            if (!(districtNamesList.contains(districtName))) {
                mRef = mDatabase.getReference().child("Districts").child(districtName);
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (Objects.equals(ds.getKey(), "color")) {
                                color = Objects.requireNonNull(ds.getValue()).toString();
                            }
                        }
                        districtInfo.add(new DistrictInfo(districtName, color));
                        drawCapturedTerritory(cLatLng, new DistrictInfo(districtName, color));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                for (int i = 0; i < districtInfo.size(); i++){
                    if (districtInfo.get(i).districtName.equals(districtName)){
                        drawCapturedTerritory(cLatLng, districtInfo.get(i));
                        break;
                    }
                }
            }
    }

    private void getCapturedTerritory(){
        mRefGetTerritories = mDatabase.getReference().child("Territory").child(cityName);
        valueEventListenerGetTerritories = mRefGetTerritories.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onUpdateAllLatLng = new ArrayList<>();
                territoryInfo = new ArrayList<>();
                for (DataSnapshot ds1 : dataSnapshot.getChildren()){
                    districtName = Objects.requireNonNull(ds1.getKey());
                    if (onStartUp){
                        differenceLatLng = new ArrayList<>();
                        for (DataSnapshot ds2 : ds1.getChildren()) {
                            onStartAllLatLng.add(convertStringToLatLng(Objects.requireNonNull(ds2.getValue()).toString()));
                            getDistrictInformation(districtName, convertStringToLatLng(Objects.requireNonNull(ds2.getValue()).toString()));
                            //drawCapturedTerritory(convertStringToLatLng(Objects.requireNonNull(ds2.getValue()).toString()));
                        }
                    } else {
                        for (DataSnapshot ds2 : ds1.getChildren()) {
                            onUpdateAllLatLng.add(convertStringToLatLng(Objects.requireNonNull(ds2.getValue()).toString()));
                            territoryInfo.add(new TerritoryInfo(districtName,convertStringToLatLng(Objects.requireNonNull(ds2.getValue()).toString())));
                        }
                    }
                }
                Log.d(TAG, "onDataChange: All polygons are listed");
                if (!onStartUp) {
                        if (onStartAllLatLng.size() < onUpdateAllLatLng.size()) {
                            differenceLatLng = new ArrayList<>(onUpdateAllLatLng);
                            differenceLatLng.removeAll(onStartAllLatLng);
                            for (int i = 0; i < territoryInfo.size(); i++){
                                if (territoryInfo.get(i).latLng.equals(differenceLatLng.get(0))){
                                    getDistrictInformation(territoryInfo.get(i).districtName, differenceLatLng.get(0));
                                    //drawCapturedTerritory(differenceLatLng.get(0));
                                    onStartAllLatLng.add(differenceLatLng.get(0));
                                }
                            }
                            Log.d(TAG, "onDataChange: Polygon has been added");
                        } else if (onStartAllLatLng.size() > onUpdateAllLatLng.size()) {
                            differenceLatLng = new ArrayList<>(onStartAllLatLng);
                            differenceLatLng.removeAll(onUpdateAllLatLng);

                            for (int e = 0; e < allCapturedPolygons.size(); e++) {
                                if (convertStringToLatLng(Objects.requireNonNull(allCapturedPolygons.get(e).getTag()).toString()).equals(differenceLatLng.get(0))) {
                                    Polygon removePolygon = allCapturedPolygons.get(e);
                                    allCapturedPolygons.remove(e);
                                    removePolygon.remove();
                                    onStartAllLatLng.remove(differenceLatLng.get(0));
                                    break;
                                }
                            }

                            Log.d(TAG, "onDataChange: Polygon has been removed");
                        }
                    }

                onStartUp = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        Log.d(TAG, "onPause: removed location listener");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRefGetTerritories.removeEventListener(valueEventListenerGetTerritories);
        Log.d(TAG, "onStop: removed valueEventListener");
    }

}
