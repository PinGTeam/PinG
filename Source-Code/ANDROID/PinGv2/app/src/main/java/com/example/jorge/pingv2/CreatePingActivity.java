/* Written by Zach and Jorge on 11/20/16 */

package com.example.jorge.pingv2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.jorge.pingv2.R.drawable.edit_btn;
import static java.lang.Math.abs;


//TODO: CHANGE THIS TO MATCH THE EVENT DETAIL
public class CreatePingActivity extends AppCompatActivity {

    //variables holding information about the event and the user
    private LatLng theCoords;
    private UserData currentUserInformation;


    //GIU stuff
    private FloatingActionButton fab;
    private TextView startTimeFieldOnScreen, endTimeFieldOnScreen;
    private EditText eventNameFieldOnScreen, eventDescFieldOnScreen;
    private int whichField;

    //time stuff
    private boolean newStartTimeSetFlag, newEndTimeFlag;
    private int newStartHour, newStartMinute, newEndHour, newEndMinute;
    private Calendar originalStartDateTime, originalEndDateTime, newStartDateTime, newEndDateTime;

    //final extracted fields for server
    private String finalEventName, finalEventDescription, finalEventStart, finalEventEnd;

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarCreatePing);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.createPingFab);

        //get info from map activity
        Bundle extras = getIntent().getExtras();
        theCoords = extras.getParcelable("theCoords");
        currentUserInformation = (UserData) getIntent().getSerializableExtra("userInformation");

        System.out.println("The coords: " + theCoords);

        //initialize time changed flags
        newStartTimeSetFlag = false;         //will only be true if the user inputs a new start time
        newEndTimeFlag = false;              //will only be true if the user inputs a new end time

        //TODO: SET TTWO GREGORIAN CALENDARS TO CURRENT DATE TIME AND USE THEM TO SET NEW END AND START DATE TIME
        //TODO: THESE WILL BE USED IN CASE A NEW TIME IS NOT CHOSEN
        TimeZone timeZone = TimeZone.getTimeZone("UTC");

        originalStartDateTime = GregorianCalendar.getInstance(timeZone);
        originalEndDateTime = GregorianCalendar.getInstance(timeZone);

        //originalStartDateTime.roll(Calendar.MONTH, 1);
        //originalEndDateTime.roll(Calendar.MONTH, 1);

        //set new start and new end to original
        newStartDateTime = new GregorianCalendar();
        //newStartDateTime = originalStartDateTime;

        newEndDateTime = new GregorianCalendar();
        //newEndDateTime = originalEndDateTime;

        //set to correct month because java's month start at 0 for some reason...
        //originalStartDateTime.add(Calendar.MONTH, 1);
        //originalEndDateTime.add(Calendar.MONTH, 1);

        System.out.println("GREGY S: " + originalStartDateTime);
        System.out.println("GREGY E: " + originalEndDateTime);

        //Set click states based on user
        InitializeInitialTimes();

        //declare fab button reference
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(CreatePingActivity.this)
                        .setTitle("Create Event")
                        .setMessage("Do you want to create this event?")

                        //AS SOON AS YOU PRESS THIS, THE DATA IN THE FIELDS ARE EXTRACTED AND SAVED FOR MIDDLE TIER
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                CompleteCreate();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(edit_btn)
                        .show();
            }
        });

        //pop up set time dialog
        startTimeFieldOnScreen = (TextView) findViewById(R.id.createPingStartTimeClickable);
        startTimeFieldOnScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichField = 1;
                showSetTimeDialog(whichField);
            }
        });
        endTimeFieldOnScreen = (TextView) findViewById(R.id.createPingEndTimeClickable);
        endTimeFieldOnScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whichField = 2;
                showSetTimeDialog(whichField);
            }
        });
    }

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
    public void InitializeInitialTimes() {

        //get references to fields
        startTimeFieldOnScreen = (TextView)findViewById(R.id.createPingStartTimeClickable);
        endTimeFieldOnScreen = (TextView)findViewById(R.id.createPingEndTimeClickable);

        ////////////////////////////////////////////////////////////////////////////////////////////
        //START TIME CONVERT------------------------------------------------------------------------
        ////////////////////////////////////////////////////////////////////////////////////////////
        //extract start time (UTC)
        int initStartTimeHour = originalStartDateTime.get(Calendar.HOUR_OF_DAY);
        int initStartTimeMinutes = originalStartDateTime.get(Calendar.MINUTE);
        System.out.println("Extracted startTime: " + initStartTimeHour + ":" + initStartTimeMinutes);

        //convert UTC to EST
        int estStartTimeHour = initStartTimeHour - 5;
        //if the conversion is negative it means we wrap around to 24
        if(estStartTimeHour < 0) {
            estStartTimeHour = abs(estStartTimeHour);
            estStartTimeHour = 24 - estStartTimeHour;
            System.out.println("EST START HOUR: " + estStartTimeHour);
        }

        //set am or pm based on hour
        String startTag;
        if(estStartTimeHour < 12)
            startTag = " AM";
        else
            startTag = " PM";

        ////////////////////////////////////////////////////////////////////////////////////////////
        //START TIME DISPLAY------------------------------------------------------------------------
        ////////////////////////////////////////////////////////////////////////////////////////////
        //if start 12 am||pm
        if(estStartTimeHour == 12 || estStartTimeHour == 0) {
            if(initStartTimeMinutes < 10) {
                String startDisplay = "12:0" + initStartTimeMinutes + startTag;
                startTimeFieldOnScreen.setText(startDisplay);
            }
            else {
                String startDisplay = "12:" + initStartTimeMinutes + startTag;
                startTimeFieldOnScreen.setText(startDisplay);
            }
        }
        //all other hours
        else {
            if(initStartTimeMinutes < 10) {
                String startDisplay = String.valueOf((estStartTimeHour % 12)) + ":0" + initStartTimeMinutes + startTag;
                startTimeFieldOnScreen.setText(startDisplay);
            }
            else {
                String startDisplay = String.valueOf((estStartTimeHour % 12)) + ":" + initStartTimeMinutes + startTag;
                startTimeFieldOnScreen.setText(startDisplay);
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        //END TIME CONVERT--------------------------------------------------------------------------
        ////////////////////////////////////////////////////////////////////////////////////////////
        //extract end time (UTC)
        int initEndTimeHour = originalEndDateTime.get(Calendar.HOUR_OF_DAY);
        int initEndTimeMinutes = originalEndDateTime.get(Calendar.MINUTE);
        System.out.println("Extracted endTime: " + initEndTimeHour + ":" + initEndTimeMinutes);

        //convert UTC to EST
        int estEndTimeHour = initEndTimeHour - 5;
        //if the conversion is negative it means we wrap around to 24
        if(estEndTimeHour < 0) {
            estEndTimeHour = abs(estEndTimeHour);
            estEndTimeHour = 24 - estEndTimeHour;
            System.out.println("EST END HOUR: " + estEndTimeHour);
        }

        String endTag;
        //set am or pm based on hour
        if(estEndTimeHour < 12)
            endTag = " AM";
        else
            endTag = " PM";

        ////////////////////////////////////////////////////////////////////////////////////////////
        //END TIME DISPLAY--------------------------------------------------------------------------
        ////////////////////////////////////////////////////////////////////////////////////////////
        //if start 12 am||pm
        if(estEndTimeHour == 12 || estEndTimeHour == 0) {
            if(initStartTimeMinutes < 10) {
                String endDisplay = "12:0" + initEndTimeHour + endTag;
                endTimeFieldOnScreen.setText(endDisplay);
            }
            else {
                String endDisplay = "12:" + initEndTimeHour + endTag;
                endTimeFieldOnScreen.setText(endDisplay);
            }
        }
        //all other hours
        else {
            if(initEndTimeMinutes < 10) {
                String endDisplay = String.valueOf((estEndTimeHour % 12)) + ":0" + initEndTimeMinutes + endTag;
                endTimeFieldOnScreen.setText(endDisplay);
            }
            else {
                String endDisplay = String.valueOf((estEndTimeHour % 12)) + ":" + initEndTimeMinutes + endTag;
                endTimeFieldOnScreen.setText(endDisplay);
            }
        }
    }

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
    public void CompleteCreate() {

        eventNameFieldOnScreen = (EditText)findViewById(R.id.createPingEventNameInput);
        eventDescFieldOnScreen = (EditText)findViewById(R.id.createPingEventDescInput);

        //extract input from name and description fields
        finalEventName = eventNameFieldOnScreen.getText().toString();
        finalEventDescription = eventDescFieldOnScreen.getText().toString();

        //only do the UTC conversions if newStartTimeSetFlags were set to true
        if(newStartTimeSetFlag) {

            //est -> utc
            int utcStartHour = newStartHour + 5;
            //if converted hour is > 24, we need to roll it over
            if(utcStartHour >= 24)
                utcStartHour %= 24;

            //now we check for cases where we should roll days
            int originalStartHour = originalStartDateTime.get(Calendar.HOUR_OF_DAY);

            ////////////////////////////////////////////////////////////////////////////////////////////
            //START CHECK-------------------------------------------------------------------------------
            ////////////////////////////////////////////////////////////////////////////////////////////
            //TODO:IF THE MONTH == 0, WE MUST EXPLICITLY SET IT TO 12
            //if newStart < originalStart : newStart day++
            if(utcStartHour < originalStartHour) {
                newStartDateTime.add(Calendar.DAY_OF_MONTH, 1);
            }
            //if newStart == original BUT newStartMinute < originalStartMinute : newStart day++
            else if(utcStartHour == originalStartHour && newStartMinute < originalStartDateTime.get(Calendar.MINUTE))
                newStartDateTime.add(Calendar.DAY_OF_MONTH, 1);

            //convert start calendar to string
            finalEventStart = String.valueOf(newStartDateTime.get(Calendar.YEAR));

            newStartDateTime.roll(Calendar.MONTH, 1);

            //if month is less than 10, but not 0
            if(newStartDateTime.get(Calendar.MONTH) < 10 && !(Objects.equals(newStartDateTime.get(Calendar.MONTH), 0))) {
                finalEventStart = finalEventStart.concat("-0" + String.valueOf(newStartDateTime.get(Calendar.MONTH)));
            }
            //if month is less than 10, but is 0, set month to december
            else if(newStartDateTime.get(Calendar.MONTH) < 10 && (Objects.equals(newStartDateTime.get(Calendar.MONTH), 0))) {
                finalEventStart = finalEventStart.concat("-12");
            }
            else
                finalEventStart = finalEventStart.concat("-" + String.valueOf(newStartDateTime.get(Calendar.MONTH)));

            if(newStartDateTime.get(Calendar.DAY_OF_MONTH) < 10)
                finalEventStart = finalEventStart.concat("-0" + String.valueOf(newStartDateTime.get(Calendar.DAY_OF_MONTH)));
            else
                finalEventStart = finalEventStart.concat("-" + String.valueOf(newStartDateTime.get(Calendar.DAY_OF_MONTH)));

            if(utcStartHour < 10)
                finalEventStart = finalEventStart.concat(" 0" + String.valueOf(utcStartHour));
            else
                finalEventStart = finalEventStart.concat(" " + String.valueOf(utcStartHour));

            if(newStartDateTime.get(Calendar.MINUTE) < 10)
                finalEventStart = finalEventStart.concat(":0" + String.valueOf(newStartDateTime.get(Calendar.MINUTE)));
            else
                finalEventStart = finalEventStart.concat(":" + String.valueOf(newStartDateTime.get(Calendar.MINUTE)));
        }
        //if no end time changes were made, then we simply send the original to the server
        else {
            //CHANGE THIS TO EXTRACTED START TIME ORIGINAL
            SimpleDateFormat startForm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            finalEventStart = startForm.format(originalStartDateTime.getTime());
        }

        //only do the UTC conversions if newEndTimeSetFlags were set to true
        if(newEndTimeFlag) {

            //est->utc
            int utcEndHour = newEndHour + 5;
            if(utcEndHour >= 24)
                utcEndHour %= 24;

            //original end hour
            int originalEndHour = originalEndDateTime.get(Calendar.HOUR_OF_DAY);

            ////////////////////////////////////////////////////////////////////////////////////////////
            //END CHECK---------------------------------------------------------------------------------
            ////////////////////////////////////////////////////////////////////////////////////////////
            //if newEnd < originalEnd : newEnd day++
            if(utcEndHour < originalEndHour) {
                newEndDateTime.add(Calendar.DAY_OF_MONTH, 1);
            }
            //if newEnd == original BUT newEndMinute < originalEndMinute : newEnd day++
            else if(utcEndHour == originalEndHour && newEndMinute < originalEndDateTime.get(Calendar.MINUTE))
                newEndDateTime.add(Calendar.DAY_OF_MONTH, 1);

            //start hour for comparison with end hour
            int utcStartHour = newStartHour + 5;

            //if converted hour is > 24, we need to roll it over
            if(utcStartHour >= 24)
                utcStartHour %= 24;

            if(utcEndHour < utcStartHour)
                newEndDateTime.add(Calendar.DAY_OF_MONTH, 1);

            //convert end calendar to string
            finalEventEnd = String.valueOf(newEndDateTime.get(Calendar.YEAR));

            newEndDateTime.roll(Calendar.MONTH, 1);

            //if end month < 10 and not 0
            if(newEndDateTime.get(Calendar.MONTH) < 10 && !(Objects.equals(newEndDateTime.get(Calendar.MONTH),0)))
                finalEventEnd = finalEventEnd.concat("-0" + String.valueOf(newEndDateTime.get(Calendar.MONTH)));
                //if end month < 10 and is 0, set month to december
            else if(newEndDateTime.get(Calendar.MONTH) < 10 && (Objects.equals(newEndDateTime.get(Calendar.MONTH),0)))
                finalEventEnd = finalEventEnd.concat("-12");
            else
                finalEventEnd = finalEventEnd.concat("-" + String.valueOf(newEndDateTime.get(Calendar.MONTH)));

            if(newEndDateTime.get(Calendar.DAY_OF_MONTH) < 10)
                finalEventEnd = finalEventEnd.concat("-0" + String.valueOf(newEndDateTime.get(Calendar.DAY_OF_MONTH)));
            else
                finalEventEnd = finalEventEnd.concat("-" + String.valueOf(newEndDateTime.get(Calendar.DAY_OF_MONTH)));

            if(utcEndHour < 10)
                finalEventEnd = finalEventEnd.concat(" 0" + String.valueOf(utcEndHour));
            else
                finalEventEnd = finalEventEnd.concat(" " + String.valueOf(utcEndHour));

            if(newEndDateTime.get(Calendar.MINUTE) < 10)
                finalEventEnd = finalEventEnd.concat(":0" + String.valueOf(newEndDateTime.get(Calendar.MINUTE)));
            else
                finalEventEnd = finalEventEnd.concat(":" + String.valueOf(newEndDateTime.get(Calendar.MINUTE)));
        }
        //if no end time changes were made, then we simply send the original to the server
        else {
            //CHANGE THIS TO EXTRACTED END TIME ORIGINAL
            SimpleDateFormat endForm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            finalEventEnd = endForm.format(originalEndDateTime.getTime());
        }

        System.out.println("eventName: " + finalEventName);
        System.out.println("startTime: " + finalEventStart);
        System.out.println("latitude: " + theCoords.latitude);
        System.out.println("endTime: " + finalEventEnd);
        System.out.println("userID: " + currentUserInformation.userID);
        System.out.println("longitude: " + theCoords.longitude);
        System.out.println("description: " + finalEventDescription);

        //call the server to post
        new CreatePing().execute();
    }

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
    public void showSetTimeDialog(final int whichField) {
        SetTimeDialog timeDialog = SetTimeDialog.newInstance(new SetTimeDialog.SetTimeDialogListener() {
            @Override
            public void onDialogPositiveClick(int hour, int minute) {

                TextView timeView = null;

                if(whichField == 1) {

                    newStartTimeSetFlag = true;     //new end time was input
                    timeView = (TextView) findViewById(R.id.createPingStartTimeClickable); //select field to print time to

                    //TODO: JAVA MONTH RANGE : 0 - 11, I INITIALLY ROLL THE MONTH OVER BY ONE TO ACCOUNT FOR THIS, SO JAN IS MONTH 1 AND NOT MONTH 0
                    //TODO: WHEN THE MONTH == 12, WE NEED TO EXPLICITLY SET IT TO 12 IN THE CHECK
                    //TODO: THIS PROBLEM WILL ALSO BE IN THE EDIT EVENT
                    //set the new startDateTime to the newly selected start time with original start year, month, and day
                    newStartDateTime.set(originalStartDateTime.get(Calendar.YEAR), originalStartDateTime.get(Calendar.MONTH), originalStartDateTime.get(Calendar.DAY_OF_MONTH), hour, minute, 00);
                    System.out.println("AFTER SELECTING NEW START TIME: " + newStartDateTime);

                    //save the values for later use
                    newStartHour = newStartDateTime.get(Calendar.HOUR_OF_DAY);
                    newStartMinute = newStartDateTime.get(Calendar.MINUTE);
                }
                else if(whichField == 2) {

                    newEndTimeFlag = true;      //new time was input
                    timeView = (TextView) findViewById(R.id.createPingEndTimeClickable);  //select field to print time to

                    //set the new endDateTime to the newly selected end time with original end year, month, and day
                    newEndDateTime.set(originalEndDateTime.get(Calendar.YEAR), originalEndDateTime.get(Calendar.MONTH), originalEndDateTime.get(Calendar.DAY_OF_MONTH), hour, minute , 00);
                    System.out.println("AFTER SELECTING NEW END TIME: " + newEndDateTime);

                    //save the values for later use
                    newEndHour = newEndDateTime.get(Calendar.HOUR_OF_DAY);
                    newEndMinute = newEndDateTime.get(Calendar.MINUTE);
                }

                //Display selected time to selected field
                String tag;
                if(hour < 12)
                    tag = " AM";
                else
                    tag = " PM";

                if(hour == 0 || hour == 12) {
                    if(minute < 10) {
                        String display = "12:0" + minute + tag;
                        timeView.setText(display);
                    }
                    else {
                        String display = "12:" + minute + tag;
                        timeView.setText(display);
                    }
                }
                else {
                    if(minute < 10) {
                        String display = (hour % 12) + ":0" + minute + tag;
                        timeView.setText(display);
                    }
                    else {
                        String display = (hour % 12) + ":" + minute + tag;
                        timeView.setText(display);
                    }
                }
            }

            @Override
            public void onDialogNegativeClick(DialogFragment dialog) {}
        });
        timeDialog.show(getSupportFragmentManager(), "Set Time");
    }

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
    private class CreatePing extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            JSONObject markerInformation = new JSONObject();
            try {
                markerInformation.put("eventName", finalEventName);
                markerInformation.put("startTime", finalEventStart);
                markerInformation.put("latitude", theCoords.latitude);
                markerInformation.put("endTime", finalEventEnd);
                markerInformation.put("userID", currentUserInformation.userID);
                markerInformation.put("longitude", theCoords.longitude);
                markerInformation.put("description", finalEventDescription);
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
