package com.example.jorge.pingv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreatePingActivity extends AppCompatActivity {

    private String pingName, pingDescription;
    private FloatingActionButton createEvent;

    private LatLng currentCoords;
    private int currentUser;

    private TextView selectedStartTime, selectedEndTime;
    private int whichField;

    private Calendar currentDateTime, startDateTime, endDateTime;
    private String currentYear, currentMonth, currentDay, currentHour, currentMinute;

    private int convYear, convMonth, convDay, convHour, convMinute;
    private int convSYear, convSMonth, convSDay, convSHour, convSMinute;
    private int convEYear, convEMonth, convEDay, convEHour, convEMinute;

    private String FINALStartDateTime, FINALEndDateTime;
    private UserData currentUserInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarCreatePing);
        setSupportActionBar(toolbar);

        InitializeTime();

        //get data from map activity
        Bundle extras = getIntent().getExtras();
        currentCoords = extras.getParcelable("theCoords");
        currentUser = extras.getInt("theUser");
        currentUserInformation = (UserData) getIntent().getSerializableExtra("userInformation");

        //pop up set time dialog
        selectedStartTime = (TextView) findViewById(R.id.createPingStartTimeClickable);
        selectedStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichField = 1;
                showSetTimeDialog(whichField);
            }
        });
        selectedEndTime = (TextView) findViewById(R.id.createPingEndTimeClickable);
        selectedEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichField = 2;
                showSetTimeDialog(whichField);
            }
        });

        //create event button press
        createEvent = (FloatingActionButton)findViewById(R.id.createPingFab);
        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("ON CREATE BUTTON Start time : " + convSHour);
                //get the data from the fields and store
                EditText eName = (EditText) findViewById(R.id.createPingEventNameInput);
                pingName = eName.getText().toString();

                EditText eDesc = (EditText) findViewById(R.id.createPingEventDescInput);
                pingDescription = eDesc.getText().toString();

                //if start hour < currentHour, increment day or if they are equal but mins are notincrement day
                if(convHour+5 > convSHour && convSHour != 0 || convMinute > convSMinute) {
                    System.out.println("HERE<<<");
                    startDateTime.roll(startDateTime.DAY_OF_MONTH, 1);
                    convSDay = startDateTime.get(startDateTime.DAY_OF_MONTH);
                    endDateTime.roll(endDateTime.DAY_OF_MONTH, 1);
                    convEDay = endDateTime.get(endDateTime.DAY_OF_MONTH);

                    FINALStartDateTime = String.valueOf(convSYear) + "-" + String.valueOf(convSMonth) + "-" + String.valueOf(convSDay) + " " + String.valueOf(convSHour) + ":" + String.valueOf(convSMinute);
                    FINALEndDateTime = String.valueOf(convEYear) + "-" + String.valueOf(convEMonth) + "-" + String.valueOf(convSDay) + " " + String.valueOf(convEHour) + ":" + String.valueOf(convEMinute);

                    System.out.println("FINAL STARTv1: " + FINALStartDateTime);
                    System.out.println("FINAL ENDv1: " + FINALEndDateTime);
                }

                if(convEHour < convSHour && convEYear != 0 || convEMinute < convSMinute) {
                    System.out.println("HEREv2<<<");
                    endDateTime.roll(endDateTime.DAY_OF_MONTH, 1);
                    //convEDay = endDateTime.get(endDateTime.DAY_OF_MONTH);

                    FINALStartDateTime = String.valueOf(convSYear) + "-" + String.valueOf(convSMonth) + "-" + String.valueOf(convSDay) + " " + String.valueOf(convSHour) + ":" + String.valueOf(convSMinute);
                    FINALEndDateTime = String.valueOf(convEYear) + "-" + String.valueOf(convEMonth) + "-" + String.valueOf(convEDay) + " " + String.valueOf(convEHour) + ":" + String.valueOf(convEMinute);
                }

                //append final values to strings
                FINALStartDateTime = String.valueOf(convSYear) + "-";
                //month check
                if(convSMonth < 10)
                    FINALStartDateTime = FINALStartDateTime.concat("0" + String.valueOf(convSMonth) + "-");
                else
                    FINALStartDateTime = FINALStartDateTime.concat(String.valueOf(convSMonth) + "-");

                //day check
                if(convSDay < 10)
                    FINALStartDateTime = FINALStartDateTime.concat("0" + String.valueOf(convSDay) + " ");
                else
                    FINALStartDateTime = FINALStartDateTime.concat(String.valueOf(convSDay) + " ");

                //hour check
                if(convSHour < 10)
                    FINALStartDateTime = FINALStartDateTime.concat("0" + String.valueOf(convSHour) + ":");
                else
                    FINALStartDateTime = FINALStartDateTime.concat(String.valueOf(convSHour) + ":");

                //minute check
                if(convSMinute < 10) {
                    FINALStartDateTime = FINALStartDateTime.concat("0" + String.valueOf(convSMinute));
                }
                else
                    FINALStartDateTime = FINALStartDateTime.concat(String.valueOf(convSMinute));

                //append final values to strings
                FINALEndDateTime = String.valueOf(convSYear) + "-";
                //month check
                if(convSMonth < 10)
                    FINALEndDateTime = FINALEndDateTime.concat("0" + String.valueOf(convEMonth) + "-");
                else
                    FINALEndDateTime = FINALEndDateTime.concat(String.valueOf(convEMonth) + "-");

                //day check
                if(convSDay < 10)
                    FINALEndDateTime = FINALEndDateTime.concat("0" + String.valueOf(convEDay) + " ");
                else
                    FINALEndDateTime = FINALEndDateTime.concat(String.valueOf(convEDay) + " ");

                //hour check
                if(convSHour < 10)
                    FINALEndDateTime = FINALEndDateTime.concat("0" + String.valueOf(convEHour) + ":");
                else
                    FINALEndDateTime = FINALEndDateTime.concat(String.valueOf(convEHour) + ":");

                //minute check
                if(convSMinute < 10) {
                    FINALEndDateTime = FINALEndDateTime.concat("0" + String.valueOf(convEMinute));
                }
                else
                    FINALEndDateTime = FINALEndDateTime.concat(String.valueOf(convEMinute));

                System.out.println("FINAL STARTv3: " + FINALStartDateTime);
                System.out.println("FINAL ENDv3: " + FINALEndDateTime);

                //post the marker to middle-tier
                new PostMarkerData().execute();
            }
        });
    }

    private void InitializeTime() {
        //get current date time
        currentDateTime = Calendar.getInstance();

        //get current year
        DateFormat currYear = new SimpleDateFormat("yyyy");
        currentYear = currYear.format(currentDateTime.getTime());
        convYear = Integer.parseInt(currentYear);

        //get current month
        DateFormat currMonth = new SimpleDateFormat("MM");
        currentMonth = currMonth.format(currentDateTime.getTime());
        convMonth = Integer.parseInt(currentMonth);

        //get current day
        DateFormat currDay = new SimpleDateFormat("dd");
        currentDay = currDay.format(currentDateTime.getTime());
        convDay = Integer.parseInt(currentDay);

        //get current hour
        DateFormat currHour = new SimpleDateFormat("HH");
        currentHour = currHour.format(currentDateTime.getTime());
        convHour = Integer.parseInt(currentHour);

        //get current minute
        DateFormat currMin = new SimpleDateFormat("mm");
        currentMinute = currMin.format(currentDateTime.getTime());
        convMinute = Integer.parseInt(currentMinute);

        int test = 0;
        test = currentDateTime.get(currentDateTime.HOUR);
        System.out.println(test);
        currentDateTime.set(convYear, convMonth, convDay, (convHour+5), convMinute);

        DateFormat currentDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currDTF = currentDateFormat.format(currentDateTime.getTime());
        System.out.println("HERE>>" + currDTF);

        startDateTime = currentDateTime;
        endDateTime = currentDateTime;
        startDateTime.set(0,0,0,0,0);
        endDateTime.set(0,0,0,0,0);
    }

    //show the dialog
    public void showSetTimeDialog(final int whichField) {
        SetTimeDialog timeDialog = SetTimeDialog.newInstance(new SetTimeDialog.SetTimeDialogListener() {
            @Override
            public void onDialogPositiveClick(int hour, int minute) {
                //now we have the hour and minute from the dialog, then just save the values to their variables

                TextView timeView = null;

                if(whichField == 1) {
                    timeView = (TextView) findViewById(R.id.createPingStartTimeClickable);

                    startDateTime.set(convYear, convMonth, convDay, hour+5, minute);

                    convSYear = startDateTime.get(startDateTime.YEAR);
                    convSMonth = startDateTime.get(startDateTime.MONTH);
                    convSDay = startDateTime.get(startDateTime.DAY_OF_MONTH);
                    convSHour = startDateTime.get(startDateTime.HOUR_OF_DAY);
                    convSMinute = startDateTime.get(startDateTime.MINUTE);
                }
                else if(whichField == 2) {
                    timeView = (TextView) findViewById(R.id.createPingEndTimeClickable);

                    endDateTime.set(convYear, convMonth, convDay, hour+5, minute);

                    convEYear = endDateTime.get(endDateTime.YEAR);
                    convEMonth = endDateTime.get(endDateTime.MONTH);
                    convEDay = endDateTime.get(endDateTime.DAY_OF_MONTH);
                    convEHour = endDateTime.get(endDateTime.HOUR_OF_DAY);
                    convEMinute = endDateTime.get(endDateTime.MINUTE);
                }

                //FOR SCREEN DISPLAY PURPOSE ONLY-------------------------------------------------------
                if(hour == 0) {
                    if(minute < 10)
                        timeView.setText("12:0" + minute + " AM");
                    else
                        timeView.setText("12:" + minute + " AM");
                }

                else if(hour < 12) {
                    if(minute < 10)
                        timeView.setText(hour + ":0" + minute + " AM");
                    else
                        timeView.setText(hour + ":" + minute + " AM");
                }
                else if(hour >= 12) {
                    if(hour == 12) {
                        if(minute < 10)
                            timeView.setText( hour + ":0" + minute + " PM");
                        else
                            timeView.setText( hour + ":" + minute + " PM");
                    }
                    else {
                        if (minute < 10)
                            timeView.setText(hour - 12 + ":0" + minute + " PM");
                        else
                            timeView.setText(hour - 12 + ":" + minute + " PM");
                    }
                }
            }

            @Override
            public void onDialogNegativeClick(DialogFragment dialog) {}
        });
        timeDialog.show(getSupportFragmentManager(), "Set Time");
    }

    private class PostMarkerData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            System.out.println("eventName: " + pingName);
            System.out.println("startTime: " + FINALStartDateTime);
            System.out.println("latitude: " + currentCoords.latitude);
            System.out.println("endTime: " + FINALEndDateTime);
            System.out.println("userID: " + currentUser);
            System.out.println("longitude: " + currentCoords.longitude);
            System.out.println("description: " + pingDescription);

            JSONObject markerInformation = new JSONObject();
            try {
                markerInformation.put("eventName", pingName);
                markerInformation.put("startTime", FINALStartDateTime);
                markerInformation.put("latitude", currentCoords.latitude);
                markerInformation.put("endTime", FINALEndDateTime);
                markerInformation.put("userID", currentUser);
                markerInformation.put("longitude", currentCoords.longitude);
                markerInformation.put("description", pingDescription);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("event", markerInformation.toString())
                    .build();

            Request request = new Request.Builder()
                    .url("http://162.243.15.139/addevent_alt")
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                if(Objects.equals(response.body().string(), "1")) {
                    System.out.println("POSTED");
                }
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        //once we finish sending data go back to map
        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(CreatePingActivity.this, "Event Created", Toast.LENGTH_SHORT).show();

            Intent gotoMapActivity = new Intent(getApplicationContext(), MapActivity.class);
            gotoMapActivity.putExtra("UserData", currentUserInformation);
            setResult(2, gotoMapActivity);
            finish();
        }
    }
}
