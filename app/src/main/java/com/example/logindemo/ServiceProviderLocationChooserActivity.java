package com.example.logindemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONObject;

import java.util.Arrays;

import afu.org.checkerframework.checker.nullness.qual.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ServiceProviderLocationChooserActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerDragListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap googleMap;
    private UiSettings uiSettings;
    private Marker currentLocationMarker = null;
    private LatLng currentLocation = null;
    private LatLng gps_location = null;
    boolean selectedLocation = false;
    FusedLocationProviderClient fusedLocationClient;
    AutocompleteSupportFragment autocompleteFragment;
    private boolean locationPermissionDenied = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider_location_chooser);

        getSupportActionBar().setTitle("Select Your Location");

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBT0IAlrJjRj9Akq7D5SltUC8SzzLMhcYE");
        }

        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_location_chooser);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {

                if (currentLocationMarker != null) {
                    currentLocationMarker.remove();
                }
                currentLocation = place.getLatLng();
                selectedLocation = true;
                Log.d("Service Provider Map", "Place " + place.toString());
                currentLocationMarker = googleMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Selected Location").draggable(true));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17.0f));
            }

            @Override
            public void onError(Status status) {

                Log.d("Service Provider Map", status.getStatusMessage());
            }

        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_location_chooser);

        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        Log.d("Second Activity", "MarkerDrag Ended");
        currentLocation = marker.getPosition();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d("Second Activity", "Marker Drag Started");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d("Second Activity", "Marker is dragged");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("in Map", "mapready");
        this.googleMap = googleMap;
        uiSettings = this.googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        this.googleMap.setOnMarkerDragListener(this);

        setUpMap();
    }


    public void setUpMap(){

        boolean result = checkLocationPermission();

        if(result){
            this.googleMap.setMyLocationEnabled(true);
            this.googleMap.setOnMyLocationClickListener(this);
            this.googleMap.setOnMyLocationButtonClickListener(this);

            this.googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                @Override
                public void onMyLocationChange(Location arg0) {

                    Log.d("Location Changed", arg0.toString());
                    gps_location = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                    if(currentLocation==null){
                        currentLocation = gps_location;
                        updateCurrentCameraAndMarker();
                    }
                }
            });

            /*
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            if (!locationPermissionDenied) {

                fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            if (currentLocation == null) {
                                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                gps_location = currentLocation;
                                updateCurrentCameraAndMarker();
                            } else
                                gps_location = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
            }*/
        }
    }

    public void updateCurrentCameraAndMarker() {

        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        currentLocationMarker = this.googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Selected Location").draggable(true));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));
        Log.d("Service Map Activity", "Added Marker at Current Location " + currentLocation.toString());

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    public boolean checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.d("Service Map Activity", "Location not enabled");
            if (ActivityCompat.shouldShowRequestPermissionRationale(ServiceProviderLocationChooserActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(ServiceProviderLocationChooserActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                return false;
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(ServiceProviderLocationChooserActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                return false;
            }
        } else {
            locationPermissionDenied = false;
            selectedLocation = true;
            Log.d("MapActivity", "Location already enabled");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpMap();
                    locationPermissionDenied = false;
                    selectedLocation = true;
                    Log.d("MapActivity", "Permission Granted");

                } else {
                    locationPermissionDenied = true;
                }
            }

        }

    }

    @Override
    public boolean onMyLocationButtonClick() {

        checkLocationPermission();
        if (currentLocation==null || !currentLocation.equals(gps_location)) {
            currentLocation = gps_location;
            updateCurrentCameraAndMarker();
        }

        Log.d("Service Map Activity", "Clicked My location Button");
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Log.d("Service Map Activity", "Clicked My Location");
    }

    public void registerServiceProvider(View v) {

        if(currentLocation==null){
            Toast.makeText(this, "Select a Location to Proceed", Toast.LENGTH_SHORT).show();
            checkLocationPermission();
        }

         else {
            //Toast.makeText(this,currentLocation.toString(),Toast.LENGTH_SHORT).show();

            String username =  getIntent().getExtras().getString("username");
            String userCategory =  getIntent().getExtras().getString("userCategory");
            String email= getIntent().getExtras().getString("email");
            String contactNumber = getIntent().getExtras().getString("contactNumber");
            String password= getIntent().getExtras().getString("password");
            String serviceCategory = getIntent().getExtras().getString("serviceCategory");
            double latitude = currentLocation.latitude;
            double longitude = currentLocation.longitude;
            CreateServiceProviderTask cP = new CreateServiceProviderTask(username,password,userCategory,serviceCategory, email, contactNumber,latitude, longitude);
            cP.execute();
        }
    }

    public class CreateServiceProviderTask extends AsyncTask<String, String, String> {

        private String username;
        private String email;
        private String contactNumber;
        private String password;
        private String userCategory;
        private String serviceCategory;
        private double latitude, longitude;

        CreateServiceProviderTask(String username, String password, String user_category, String service_category, String email, String contact_number, double latitude, double longitude) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.contactNumber = contact_number;
            this.userCategory = user_category;
            this.serviceCategory = service_category;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected String doInBackground(String... params) {

            String result;

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", password);
                jsonObject.put("email", email);
                jsonObject.put("contactNumber", contactNumber);
                jsonObject.put("userCategory", userCategory);
                jsonObject.put("serviceCategory", serviceCategory);
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);


                HttpConnection httpConnection = new HttpConnection();

                result = httpConnection.doPostRequest("serviceProvider/signup", jsonObject);

                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null) {
                Log.d("Service Registration", "Some error has occurred at Server");
                Toast.makeText(ServiceProviderLocationChooserActivity.this, "Some error occured. Try again", Toast.LENGTH_LONG).show();
            } else if (s.equals("fail")) {
                Log.e("Service Register", "Could not add the data");
                Toast.makeText(ServiceProviderLocationChooserActivity.this, "Something went wrong. Try again", Toast.LENGTH_LONG).show();
            } else {

                Log.d("Service Registration", "User is Registered Display the Login page");

                Toast.makeText(ServiceProviderLocationChooserActivity.this, "Successfully Registered", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ServiceProviderLocationChooserActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            Log.d("Service Registration", "Registration Task Cancelled");
        }
    }

}