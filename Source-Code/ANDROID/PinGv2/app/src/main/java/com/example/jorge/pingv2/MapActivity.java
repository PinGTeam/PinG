package com.example.jorge.pingv2;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private LatLng theCoords;
    private boolean firstLoad;
    private GoogleApiClient mApiClient;
    private LocationRequest mLocReq;
    private Button userProfileButton;
    private String allEventsString;
    private String eName, eDescription, eStartTime, eEndTime;
    private String userID, userFname, userLname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstLoad = true;

        if(checkGoogleServices()) {
            setContentView(R.layout.activity_map);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


            userProfileButton = (Button) findViewById(R.id.userProfileButton);
            userProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent startUserProfile = new Intent(getApplicationContext(), UserProfileActivity.class);
                    startActivity(startUserProfile);
                }
            });

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent startCreateEvent = new Intent(getApplicationContext(), CreateEventActivity.class);
                    startActivity(startCreateEvent);
                }
            });

        }
        else {
            finish();
        }
    }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setMaxZoomPreference(18);
        mMap.setMinZoomPreference(18);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(false);

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
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent startCreateEvent = new Intent(getApplicationContext(), CreateEventActivity.class);
        startActivity(startCreateEvent);
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

            if(firstLoad == true) {
                moveToLocation(loc.latitude, loc.longitude, 18);
                firstLoad = false;
            }

            new GetMarkerData().execute();
        }
    }

    private void moveToLocation(double latitude, double longitude, int zoom) {
        LatLng location = new LatLng(latitude, longitude);
        CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(location, zoom);
        mMap.moveCamera(cam);
    }

    //post marker information to middle tier
    private class PostMarkerData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            //System.out.println("Coords Lat: " + theCoords.latitude + " Coords Lng: " + theCoords.longitude);

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
                properties.put("startTime", eStartTime);
                properties.put("endTime", eEndTime);
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
