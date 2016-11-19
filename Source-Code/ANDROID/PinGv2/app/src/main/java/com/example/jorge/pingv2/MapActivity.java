package com.example.jorge.pingv2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.InfoWindowAdapter {

    SupportMapFragment sMapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mApiClient;
    private boolean firstLoad, firstread;
    private LocationRequest mLocReq;
    private LatLng theCoords;
    private int theUserID;
    private String allEventsString;
    private ArrayList<EventModel> listOfMarkers;
    private UserData currentUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sMapFragment = SupportMapFragment.newInstance();
        setContentView(R.layout.activity_map);

        //get user class from login
        currentUserInfo = (UserData) getIntent().getSerializableExtra("UserData");
        theUserID = currentUserInfo.userID;

        System.out.println("USERID: " + theUserID);

        //set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set ping button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateEvent = new Intent(getApplicationContext(), CreateEventActivity.class);

                //send info to ping creation screen
                Bundle createPingInfo = new Bundle();
                createPingInfo.putParcelable("theCoords", theCoords);
                createPingInfo.putInt("theUser", theUserID);
                startCreateEvent.putExtras(createPingInfo);
                startActivity(startCreateEvent);
            }
        });

        //flying nav bar and map placement
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        sMapFragment.getMapAsync(this);

        android.app.FragmentManager mapFragmentManager = getFragmentManager();
        android.support.v4.app.FragmentManager sFM = getSupportFragmentManager();

        sFM.beginTransaction().add(R.id.map, sMapFragment).commit();

        firstLoad = true;
    }

    //closes nav bar when back is pressed
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //set elements on navbar and set text on nav bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);

        TextView sideUserName = (TextView)findViewById(R.id.sideBarUserFirstLast);
        sideUserName.setText(currentUserInfo.firstName + " " + currentUserInfo.lastName);

        TextView sideUserID = (TextView)findViewById(R.id.sideBarUserName);
        sideUserID.setText(currentUserInfo.userName);

        return true;
    }

    //for later iterations
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //for later iterations
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*
        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //map is loaded, set preferences
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(this);
        //mMap.setMaxZoomPreference(18);
        //mMap.setMinZoomPreference(18);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        //mMap.getUiSettings().setZoomGesturesEnabled(false);

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

    //every time the location is changed
    @Override
    public void onLocationChanged(Location location) {
        //check for location
        if(location == null) {
            Toast.makeText(this, "Cannot get current location", Toast.LENGTH_LONG).show();
        }

        //if we find it, allow the placement of marker
        else {
            final LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

            //save current coordinates
            theCoords = loc;
            System.out.println(theCoords);

            //on first load, jump to current location
            if(firstLoad == true) {
                moveToLocation(loc.latitude, loc.longitude, 18);
                firstLoad = false;
            }

            //call async task to retrieve pings from database
            new GetMarkers().execute();
        }
    }

    //jumps to location
    private void moveToLocation(double latitude, double longitude, int zoom) {
        LatLng location = new LatLng(latitude, longitude);
        CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(location, zoom);
        mMap.moveCamera(cam);
    }

    //will implement event edit screen
    @Override
    public void onInfoWindowClick(Marker marker) {
        //TODO: when clicked go to edit event screen
        //Toast.makeText(this, "CLICKED THE INFO WINDOW", Toast.LENGTH_SHORT).show();
        EventModel currMarker = (EventModel) marker.getTag();
        Toast.makeText(this, (int) currMarker.userID, Toast.LENGTH_SHORT).show();


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
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public View getInfoWindow(Marker marker) {
        //View view = getLayoutInflater().inflate(R.layout.custom_event_info_window, null);
        // return view;
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.custom_event_info_window, null);
        return view;
    }

    //called at end of location changed, will update map with new pings
    private class GetMarkers extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            HttpUrl url = HttpUrl.parse("http://162.243.15.139/getnearevents_alt");
            HttpUrl.Builder myBuilder = url.newBuilder();
            myBuilder.addQueryParameter("longitude", String.valueOf(theCoords.longitude));
            myBuilder.addQueryParameter("latitude", String.valueOf(theCoords.latitude));

            Request request = new  Request.Builder()
                    .url(myBuilder.build())
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                allEventsString = response.body().string();

                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        //after I get back all the events from middle tier, save them in array and then place them on the map
        @Override
        protected void onPostExecute(Void aVoid) {
            //listOfMarkers = null;
            listOfMarkers = EventModel.fromArrayJson(allEventsString);
            for(int i = 0; i < listOfMarkers.size(); i++) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(listOfMarkers.get(i).longitude, listOfMarkers.get(i).latitude))
                        .title(listOfMarkers.get(i).eventName)
                        .snippet(listOfMarkers.get(i).description));
                marker.setTag(listOfMarkers.get(i));
            }
        }
    }
}
