package com.example.jorge.pingv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateEventActivity extends AppCompatActivity {

    private String eventName, eventDescription, eventStartTime, eventEndTime;
    private Button createEvent;
    private LatLng currentCoords;
    private String currentUser; //DOES THIS HAVE TO BE AN INT
    private TextView selectStartTime, selectEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        //TODO: get Latlng and userID from mapactivity
        Bundle extras = getIntent().getExtras();
        currentCoords = extras.getParcelable("theCoords");
        currentUser = extras.getString("theUser");

        System.out.println("USERID: " + currentUser);
        System.out.println("LAT: " + currentCoords.latitude + "   |   LONG: " + currentCoords.longitude);

        selectStartTime = (TextView) findViewById(R.id.eventStartInput);
        selectStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectTimeDialog timeWindow = new SelectTimeDialog();
                timeWindow.show(getSupportFragmentManager(), "dialog_select_time_dialog");
            }
        });

        createEvent = (Button)findViewById(R.id.createEventButton);
        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText eName = (EditText) findViewById(R.id.eventNameInput);
                eventName = eName.getText().toString();

                EditText eDesc = (EditText) findViewById(R.id.eventDescriptionInput);
                eventDescription = eDesc.getText().toString();

                TextView eStart = (TextView) findViewById(R.id.eventStartInput);
                eventStartTime = eStart.getText().toString();

                TextView eEnd = (TextView) findViewById(R.id.eventEndInput);
                eventEndTime = eEnd.getText().toString();

                //TODO: convert time UTC before sending it in

                new PostMarkerData().execute();

                Intent startMapActivity = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(startMapActivity);
            }
        });
    }



    private class PostMarkerData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            JSONObject geometry = new JSONObject();
            try{
                JSONArray pingLatLng = new JSONArray("[" + currentCoords.latitude + ", " + currentCoords.longitude + "]");
                geometry.put("coordinates", pingLatLng);
                geometry.put("type", "Point");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject properties = new JSONObject();
            try {
                properties.put("userID", currentUser);
                properties.put("description", eventDescription);
                properties.put("eventName", eventName);
                properties.put("startTime", eventStartTime);
                properties.put("endTime", eventEndTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject markerInformation = new JSONObject();
            try {
                markerInformation.put("geometry", geometry);
                markerInformation.put("properties", properties);
                markerInformation.put("type", "Feature");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("event", markerInformation.toString())
                    .build();

            Request request = new Request.Builder()
                    .url("http://162.243.15.139/addevent")
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                if(Objects.equals(response.body().string(), "1")) {
                    //TODO: Do we add our new event on success to our array here?
                    System.out.println("POSTED");
                }
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
