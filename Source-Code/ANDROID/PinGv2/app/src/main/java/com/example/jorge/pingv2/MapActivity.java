package com.example.jorge.pingv2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static java.lang.Math.abs;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.InfoWindowAdapter {

    //map variables
    private SupportMapFragment sMapFragment;
    private GoogleApiClient mApiClient;
    private LocationRequest mLocReq;
    private LatLng theCoords;
    private GoogleMap mMap;

    private boolean firstLoad;

    //userData variables
    private UserData currentUserInfo;
    private int theUserID;

    //middle-tier response variables
    private String allEventsString;
    private ArrayList<EventModel> listOfMarkers;
    private HashMap<Integer, EventModel> markerHash;

    //current marker variables
    private long clickedMarkerID;
    private String clickedMarkerInfo;
    private EventModel clickedMarkerParsedEvent;


    //----------------------------------------------------------------------------------------------
    //STATE/INIT METHODS----------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sMapFragment = SupportMapFragment.newInstance();
        setContentView(R.layout.activity_map);

        //get user class from login
        currentUserInfo = (UserData) getIntent().getSerializableExtra("UserData");
        theUserID = currentUserInfo.userID;

        //set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set ping button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startCreateEvent = new Intent(getApplicationContext(), CreatePingActivity.class);

                //send info to ping creation screen, carry userInfo with us
                Bundle createPingInfo = new Bundle();
                createPingInfo.putParcelable("theCoords", theCoords);
                startCreateEvent.putExtras(createPingInfo);
                startCreateEvent.putExtra("userInformation", currentUserInfo);

                startActivityForResult(startCreateEvent, 2);
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

        //android.app.FragmentManager mapFragmentManager = getFragmentManager();
        android.support.v4.app.FragmentManager sFM = getSupportFragmentManager();

        sFM.beginTransaction().add(R.id.map, sMapFragment).commit();

        firstLoad = true;
    }

    //returning from another activity, if condition is met we refresh the map to add ping
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2) {
            if(data != null) {
                new GetMarkers().execute();
            }
        }
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

    //detects location update
    @Override
    public void onLocationChanged(Location location) {
        //check for location
        if (location == null) {
            Toast.makeText(this, "Cannot get current location", Toast.LENGTH_LONG).show();
        }

        //if we find it, allow the placement of marker
        else {
            final LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

            //save current coordinates;
            theCoords = loc;

            //on first load, jump to current location and load local pings
            if (firstLoad) {
                moveToLocation(loc.latitude, loc.longitude, 18);
                new GetMarkers().execute();
                firstLoad = false;
            }

            //call async task to retrieve pings from database if we have moved a certain amount
            if((theCoords.latitude > theCoords.latitude+0.0010498) || (theCoords.latitude < theCoords.latitude-0.0010498) &&
                    (theCoords.longitude > theCoords.longitude+0.0006866) || (theCoords.longitude < theCoords.longitude-0.0006866)) {
                new GetMarkers().execute();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocReq = LocationRequest.create();
        mLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocReq.setInterval(1000);

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

    //----------------------------------------------------------------------------------------------
    //HELPER METHODS--------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    //jumps to location
    private void moveToLocation(double latitude, double longitude, int zoom) {
        LatLng location = new LatLng(latitude, longitude);
        CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(location, zoom);
        mMap.moveCamera(cam);
    }

    //----------------------------------------------------------------------------------------------
    //NAVBAR METHODS--------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    //set elements on navbar and set text on nav bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);

        TextView sideUserName = (TextView) findViewById(R.id.sideBarUserFirstLast);
        sideUserName.setText(currentUserInfo.firstName + " " + currentUserInfo.lastName);

        TextView sideUserID = (TextView) findViewById(R.id.sideBarUserName);
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
            Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
            Intent theIntent = new Intent(getApplicationContext(), LogInActivity.class);
            theIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(theIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //for later iterations
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //----------------------------------------------------------------------------------------------
    //MARKER METHODS--------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    //will implement event edit screen
    @Override
    public void onInfoWindowClick(Marker marker) {
        int markerHashIndex = Integer.parseInt(marker.getTitle());
        EventModel currentMarker = markerHash.get(markerHashIndex);
        clickedMarkerID = currentMarker.eventID;

        new RetrieveMarkerInfo().execute();

        Bundle extras = new Bundle();
        extras.putInt("userID", theUserID);
        extras.putLong("eventID", clickedMarkerID);

        marker.hideInfoWindow();

        Intent startEditActivity = new Intent(getApplicationContext(), EventDetailsActivity.class);
        startEditActivity.putExtras(extras);
        startEditActivity.putExtra("markerInfo", currentMarker);
        startEditActivity.putExtra("userInfo", currentUserInfo);
        startActivityForResult(startEditActivity, 2);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.custom_event_info_window, null);
        int currentMarkerID = Integer.parseInt(marker.getTitle());
        EventModel currentMarker = markerHash.get(currentMarkerID);

        TextView eName = (TextView) view.findViewById(R.id.eventNameField);
        eName.setText(currentMarker.eventName);

        TextView uName = (TextView) view.findViewById(R.id.userName);
        uName.setText(currentMarker.firstName + " " + currentMarker.lastName);

        TextView startTimeOnScreen = (TextView) view.findViewById(R.id.startTimeField);
        TextView endTimeOnScreen = (TextView) view.findViewById(R.id.endTimeField);

        Calendar startDateTime, endDateTime;
        //convert startTime string to Calendar
        DateFormat startDT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        startDateTime = Calendar.getInstance();
        try {
            startDateTime.setTime(startDT.parse(currentMarker.startTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //convert endTime string to Calendar
        DateFormat endDT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        endDateTime  = Calendar.getInstance();
        try {
            endDateTime.setTime(endDT.parse(currentMarker.endTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //utc stuff
        int startH = startDateTime.get(Calendar.HOUR_OF_DAY) -5;
        int startM = startDateTime.get(Calendar.MINUTE);

        if(startH < 0) {
            startH = abs(startH);
            startH = 24 - startH;
        }

        String startTag;
        if(startH < 12)
            startTag = " AM";
        else
            startTag = " PM";

        ////////////////////////////////////////////////////////////////////////////////////////////
        //START TIME DISPLAY------------------------------------------------------------------------
        ////////////////////////////////////////////////////////////////////////////////////////////
        //if start 12 am||pm
        if(startH == 12 || startH == 0) {
            if(startM < 10) {
                String startDisplay = "12:0" + startM + startTag;
                startTimeOnScreen.setText(startDisplay);
            }
            else {
                String startDisplay = "12:" + startM + startTag;
                startTimeOnScreen.setText(startDisplay);
            }
        }
        //all other hours
        else {
            if(startM < 10) {
                String startDisplay = String.valueOf((startH % 12)) + ":0" + startM + startTag;
                startTimeOnScreen.setText(startDisplay);
            }
            else {
                String startDisplay = String.valueOf((startH % 12)) + ":" + startM + startTag;
                startTimeOnScreen.setText(startDisplay);
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        //END TIME CONVERT--------------------------------------------------------------------------
        ////////////////////////////////////////////////////////////////////////////////////////////
        //if the conversion is negative it means we wrap around to 24
        int endH = endDateTime.get(Calendar.HOUR_OF_DAY) -5;
        int endM = endDateTime.get(Calendar.MINUTE);

        if(endH < 0) {
            endH = abs(endH);
            endH = 24 - endH;
            System.out.println("EST END HOUR: " + endH);
        }

        String endTag;
        //set am or pm based on hour
        if(endH < 12)
            endTag = " AM";
        else
            endTag = " PM";

        ////////////////////////////////////////////////////////////////////////////////////////////
        //END TIME DISPLAY--------------------------------------------------------------------------
        ////////////////////////////////////////////////////////////////////////////////////////////
        //if start 12 am||pm
        if(endH == 12 || endH == 0) {
            if(endM < 10) {
                String endDisplay = "12:0" + endH + endTag;
                endTimeOnScreen.setText(endDisplay);
            }
            else {
                String endDisplay = "12:" + endH + endTag;
                endTimeOnScreen.setText(endDisplay);
            }
        }
        //all other hours
        else {
            if(endM < 10) {
                String endDisplay = String.valueOf((endH % 12)) + ":0" + endM + endTag;
                endTimeOnScreen.setText(endDisplay);
            }
            else {
                String endDisplay = String.valueOf((endH % 12)) + ":" + endM + endTag;
                endTimeOnScreen.setText(endDisplay);
            }
        }

        TextView eDesc = (TextView) view.findViewById(R.id.eventDescriptionField);
        eDesc.setText(currentMarker.description);

        return view;
    }

    //----------------------------------------------------------------------------------------------
    //ASYNC METHODS---------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    //called at end of location changed, will update map with new pings
    private class GetMarkers extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            System.out.println("LatLong: " + theCoords.latitude + ", " + theCoords.longitude);
            System.out.println("UserID: " + currentUserInfo.userID);

            HttpUrl url = HttpUrl.parse("http://162.243.15.139/getnearevents_alt");
            HttpUrl.Builder myBuilder = url.newBuilder();
            myBuilder.addQueryParameter("longitude", String.valueOf(theCoords.longitude));
            myBuilder.addQueryParameter("latitude", String.valueOf(theCoords.latitude));
            myBuilder.addQueryParameter("userID", String.valueOf(theUserID));

            Request request = new Request.Builder()
                    .url(myBuilder.build())
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                allEventsString = response.body().string();

                System.out.println("SERVER RESPONSE: " + allEventsString);

                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        //after I get back all the events from middle tier, save them in array and then place them on the map
        @Override
        protected void onPostExecute(Void aVoid) {
            listOfMarkers = new ArrayList<>();
            listOfMarkers = EventModel.fromArrayJson(allEventsString);
            markerHash = new HashMap<>();
            for (int i = 0; i < listOfMarkers.size(); i++) {

                String convID = String.valueOf(i);

                mMap.addMarker(new MarkerOptions()
                        .title(convID)
                        .position(new LatLng(listOfMarkers.get(i).longitude, listOfMarkers.get(i).latitude))
                        .snippet(listOfMarkers.get(i).description));

                markerHash.put(i, listOfMarkers.get(i));
            }
        }
    }

    private class RetrieveMarkerInfo extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            HttpUrl url = HttpUrl.parse("http://162.243.15.139/getevent_alt");
            HttpUrl.Builder myBuilder = url.newBuilder();
            myBuilder.addQueryParameter("eventID", String.valueOf(clickedMarkerID));
            myBuilder.addQueryParameter("userID", String.valueOf(theUserID));

            Request request = new Request.Builder()
                    .url(myBuilder.build())
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                clickedMarkerInfo = response.body().string();

                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            clickedMarkerParsedEvent = EventModel.fromJson(clickedMarkerInfo);
        }
    }
}
