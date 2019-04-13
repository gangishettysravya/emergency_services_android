package com.example.logindemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import afu.org.checkerframework.checker.nullness.qual.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class CitizenHomeActivity extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerDragListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap googleMap;
    private UiSettings uiSettings;
    boolean locationChanged = false;
    private Marker currentLocationMarker=null;
    private LatLng currentLocation=null;
    private LatLng gps_location=null;
    AutocompleteSupportFragment autocompleteFragment;
    List <marker> servicesAvailableMarkers;
    private boolean locationPermissionDenied = true;
    double distance;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_map, container, false);
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view,savedInstanceState);
        getActivity().setTitle("Select Your Location");

        //To get the search bar

        if(!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), "APIKEY");
        }

        servicesAvailableMarkers = new ArrayList<marker>();

        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place){

                currentLocation = place.getLatLng();
                Log.d("Map Activity","Place "+place.toString());
                updateCurrentCameraAndMarker();
                getNearestServices();
            }

            @Override
            public void onError(Status status){

                Log.d("Map Activity",status.getStatusMessage());

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);


        Button requestButton = (Button) view.findViewById(R.id.btnRequest);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLocation!=null && servicesAvailableMarkers.size()!=0){

                    Intent intent = new Intent(getActivity(), CitizenRequest.class);
                    intent.putExtra("latitude",currentLocation.latitude);
                    intent.putExtra("longitude",currentLocation.longitude);
                    intent.putExtra("distance",distance);
                    startActivity(intent);
                }
                else if(locationPermissionDenied){
                    Toast.makeText(getContext(),"Need to select a location", Toast.LENGTH_SHORT).show();
                    checkLocationPermission(getContext());
                }
                else if(servicesAvailableMarkers.size()==0){
                    Toast.makeText(getContext(),"Sorry! No Services Available in this Area", Toast.LENGTH_SHORT).show();
                }
            }
        });
        distance = 10;
    }

    @Override
    public void onMarkerDragEnd(Marker marker){

        Log.d("Second Activity","MarkerDrag Ended");
        currentLocation = marker.getPosition();
        updateCurrentCameraAndMarker();
        getNearestServices();
    }

    @Override
    public void onMarkerDragStart(Marker marker){
        Log.d("Second Activity","Marker Drag Started");
    }

    @Override
    public  void onMarkerDrag(Marker marker){
        Log.d("Second Activity","Marker is dragged");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("in Map", "mapready");
        this.googleMap = googleMap;
        uiSettings = this.googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        this.googleMap.setOnMarkerDragListener(this);
        setupMap();
    }

    public void setupMap() {

        boolean result = checkLocationPermission(getContext());
        if (result){
            uiSettings.setMyLocationButtonEnabled(true);
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
                        getNearestServices();
                        updateCurrentCameraAndMarker();
                        locationChanged = true;
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
                                getNearestServices();
                                updateCurrentCameraAndMarker();
                            } else
                                gps_location = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
            }*/
        }
    }

    public void getNearestServices(){
        if(currentLocation!=null){
            NearestServicesTask nT = new NearestServicesTask(currentLocation.latitude,currentLocation.longitude);
            nT.execute();
        }
    }

    public void showNearestServices() {

        for (int i = 0; i < servicesAvailableMarkers.size(); i++) {
            marker service = servicesAvailableMarkers.get(i);
            double latitude = service.getLatitude();
            double longitude = service.getLongitude();

        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latitude, longitude)).title(service.getType());

        if(service.getType().equals("Police Station"))
          {
              BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.ic_police_marker);
              Bitmap bitmap=bitmapdraw.getBitmap();
              Bitmap hospital_marker = Bitmap.createScaledBitmap(bitmap,150,150, false);
              markerOptions.icon(BitmapDescriptorFactory.fromBitmap(hospital_marker));

          }
            else if(service.getType().equals("Hospital")) {
              BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.ic_hospital_marker);
              Bitmap bitmap=bitmapdraw.getBitmap();
              Bitmap hospital_marker = Bitmap.createScaledBitmap(bitmap,150,150, false);
              markerOptions.icon(BitmapDescriptorFactory.fromBitmap(hospital_marker));
            }
           else if(service.getType().equals("Fire Station")){
              BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.ic_firestation_marker);
              Bitmap bitmap=bitmapdraw.getBitmap();
              Bitmap hospital_marker = Bitmap.createScaledBitmap(bitmap,150,150, false);
              markerOptions.icon(BitmapDescriptorFactory.fromBitmap(hospital_marker));
          }
        this.googleMap.addMarker(markerOptions);
        Log.d("CitizenHomeActivity", "Added marker : " + service.getType());

        }

    }


    public  void updateCurrentCameraAndMarker(){

        this.googleMap.clear();
        distance = 10;

        currentLocationMarker = this.googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(currentLocation).title("Selected Location").draggable(true));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));
        Log.d("Second Activity","Added Marker at Current Location " + currentLocation.toString());

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    public boolean checkLocationPermission(final Context context){

        if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            Log.d("SecondActivity","Location not enabled");
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
                return false;
            }
            else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
                return false;
            }
        }

        else {

            Log.d("MapActivity","Location already enabled");
            locationPermissionDenied = false;
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int [] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupMap();
                    Log.d("MapActivity","Permission Granted");
                    locationPermissionDenied = false;
                }
                else {
                    locationPermissionDenied = true;
                }
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {

        if(currentLocation!=null) {
            //checkLocationPermission(getContext());
            if (!currentLocation.equals(gps_location)) {
                currentLocation = gps_location;
                updateCurrentCameraAndMarker();
                getNearestServices();
            }
            Log.d("Map Activity", "Clicked My location Button");
        }
        else{
            Toast.makeText(getContext(),"Wait for the location to be updated",Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location){

        Log.d("Map Activity","Clicked My Location");
    }


    public class NearestServicesTask extends AsyncTask<String, String, String> {

        private double latitude;
        private double longitude;

        NearestServicesTask(double latitude,double longitude){
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO: attempt authentication against a network service.

            String result;

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitude",latitude);
                jsonObject.put("longitude",longitude);
                jsonObject.put("distance",distance);

                HttpConnection httpConnection = new HttpConnection();

                result = httpConnection.doPostRequest("citizen/getNearestServices",jsonObject);

                if(result!=null)
                    if(result.equals("[]")){
                    return null;
                }

                return result;

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s == null)
            {
                servicesAvailableMarkers.clear();
                showNearestServices();
                if(distance==10) {
                    Toast.makeText(getContext(), "No Services Available within " + distance + "Km. Searching for Services within 15 Km", Toast.LENGTH_LONG).show();
                    distance = 15;
                    NearestServicesTask Ts = new NearestServicesTask(latitude,longitude);
                    Ts.execute();
                }

                else if(distance==15){
                    Toast.makeText(getContext(), "No Services Available within " + distance + "Km.",Toast.LENGTH_LONG).show();
                }

            }
            else {

                Log.d("CitizenHomeActivity", "Services Received " + s);

                Toast.makeText(getContext(),"Services Available within " + distance + "Km.",Toast.LENGTH_SHORT).show();
                servicesAvailableMarkers.clear();

                try {
                    JSONArray nearest_services = new JSONArray(s);
                    for(int i=0;i<nearest_services.length();i++){
                        JSONObject service = nearest_services.getJSONObject(i);
                        marker m = new marker(service.getDouble("latitude"),service.getDouble("longitude"),service.getString("serviceCategory"));
                        servicesAvailableMarkers.add(m);
                 }

                    showNearestServices();
                }

                catch(Exception e){
                    Log.d("CitizenHomeActivity",e.getMessage());
                }

            }
        }

        @Override
        protected void onCancelled() {
            Log.d("RegistrationActivity","Registration Task Cancelled");
        }
    }
}

class marker{

    private double latitude;
    private double longitude;
    private String type; //Describes the type

    marker(double lat,double longt,String type){
        this.latitude = lat;
        this.longitude = longt;
        this.type = type;
    }

    double getLongitude(){
        return this.longitude;
    }

    double getLatitude(){
        return this.latitude;
    }

    void setLatitude(double latitude){
        this.latitude = latitude;
    }

    void setLongitude(double longitude){
        this.longitude = longitude;
    }

    void setType(String type){
        this.type = type;
    }

    String getType(){
        return this.type;
    }

}
