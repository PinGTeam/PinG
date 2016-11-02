package com.example.jorge.pingv2;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener,
        MarkerDialog.OnDialogClickListener {

    private GoogleMap mMap;
    private Marker mMarker;
    private LocationRequest mLocReq;
    private GoogleApiClient mApiClient;
    private LatLng theCoords, topLeftCoords, bottomRightCoords;

    private String eName, eDescription, eTime;
    private String userID, userFname, userLname;
    private String allEventsString;

    JSONArray geoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get userID from logIn activity
        Intent data = getIntent();
        userID = data.getStringExtra("key");


        //check if google play service is available
        if (checkGoogleServices()) {
            setContentView(R.layout.activity_map);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            //make pinG button
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //create and show custom dialog
                    MarkerDialog eventWindow = new MarkerDialog();
                    eventWindow.show(getSupportFragmentManager(), "dialog_marker_dialog");
                }
            });
        }
        else {
            finish();
        }
    }

    //Check Google Play Services Availability
    private boolean checkGoogleServices() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS)
            return true;
        else if (api.isUserResolvableError(isAvailable)) {
            Dialog box = api.getErrorDialog(this, isAvailable, 0);
            box.show();
        } else {
            Toast.makeText(this, "Cannot connect to Google Play Services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    //when the map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(18);
        mMap.setMinZoomPreference(18);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(true);   //change this to true
        mMap.getUiSettings().setZoomGesturesEnabled(false);    //

        //check if we can access location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        //build api client and connect to it
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocReq = LocationRequest.create();
        mLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocReq.setInterval(100000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocReq, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {

        //check for location
        if(location == null) {
            Toast.makeText(this, "Cannot get current location", Toast.LENGTH_LONG).show();
        }

        //if we find it, allow the placement of marker
        else {
            final LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

            //save coordinates here
            theCoords = loc;

            //get screen corners
            VisibleRegion vRegion = mMap.getProjection().getVisibleRegion();
            topLeftCoords = vRegion.farLeft;
            bottomRightCoords = vRegion.nearRight;

            //they seem to be correct <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            //System.out.println("TopLeft: " + topLeftCoords + "BottomRight: "+ bottomRightCoords);

            moveToLocation(loc.latitude, loc.longitude, 18);

            new GetMarkerData().execute();
        }
    }

    //move to location
    private void moveToLocation(double latitude, double longitude, int zoom) {
        LatLng location = new LatLng(latitude, longitude);
        CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(location, zoom);
        mMap.moveCamera(cam);
    }

    //set marker with data from creation prompt
    private void setMarker(String title, String time, String snip, double lat, double lng) {
        //remove previous marker
        if (mMarker != null) {
            mMarker.remove();
        }
        //new marker properties
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(title + " : " + time)
                .snippet(snip)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        mMarker = mMap.addMarker(options);
    }

    //get data from even creation prompt
    @Override
    public void onDialogPositiveClick(String name, String time, String desc) {
        eName = name;
        eTime = time;
        eDescription = desc;

        //correct coordinates stored
        System.out.println(theCoords.latitude + ":" + theCoords.longitude);

        //set marker on the map
        setMarker( eName, eTime, eDescription, theCoords.latitude, theCoords.longitude);

        //send marker data to middle tier
        //new PostMarkerData().execute();
    }

    //post marker information to middle tier
    private class PostMarkerData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            System.out.println("Coords Lat: " + theCoords.latitude + " Coords Lng: " + theCoords.longitude);

            JSONObject geometry = new JSONObject();
            try {
                JSONArray coord = new JSONArray("[" + theCoords.longitude + ", " + theCoords.latitude + "]");
                //need to get rid of the "" around coordinates

                geometry.put("coordinates", coord);
                geometry.put("type", "Point");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject properties = new JSONObject();
            try {
                properties.put("description", eDescription);
                properties.put("eventName", eName);
                properties.put("time", eTime);
                properties.put("userID", userID);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject geoJSON = new JSONObject();
            try {
                geoJSON.put("geometry", geometry);
                geoJSON.put("properties", properties);
                geoJSON.put("type", "Feature");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("location", geoJSON.toString())
                    .build();

            Request request = new Request.Builder()
                    .url("http://162.243.15.139/addevent")
                    .post(formBody)
                    .build();

            System.out.println(geoJSON.toString());

            try {
                Response response= client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                //store the response [NOTE: DO NOT USE response.body() BEFORE THIS LINE BECAUSE IT WILL CONSUME THE RETURN]
                if(Objects.equals(response.body().string(), "hello")) {
                    System.out.println("POSTED");
                }
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    //get marker information from middle tier
    private class GetMarkerData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();
            Request request = new  Request.Builder()
                    .url("http://162.243.15.139/getallevents")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                allEventsString = response.body().string();
                System.out.println("ALL EVENTS: " + allEventsString);

                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                JSONObject obj = new JSONObject(allEventsString);
                JSONArray points = obj.getJSONArray("features");

                for(int i = 0; i < points.length(); i++) {
                    JSONObject marker = points.getJSONObject(i);

                    System.out.println("EventName: " + marker.getString("geometry"));
                }
                //geoData is JSONArray
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
