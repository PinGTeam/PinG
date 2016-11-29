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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.jorge.pingv2.R.drawable.edit_btn;
import static java.lang.Math.abs;

public class EventDetailsActivity extends AppCompatActivity {

    //info stuff
    private int userID;
    private long eventID;
    private EventModel markerInformation;
    private UserData currentUserInformation;

    private boolean firstLoad;

    //users attending stuff
    private Switch attendSwitch;            //the actual switch itself
    private boolean attendingFlag;          //set true if the user is attending
    private int attendanceCount;            //how many attendees
    private String attendanceResponse;      //server response
    private String allAttendingUsers;       //JSON of attendees
    private ArrayList<UserData> attendingUsersList; //Array of attendees

    //GIU stuff
    private FloatingActionButton fab;
    private TextView startTimeFieldOnScreen, endTimeFieldOnScreen, attendanceCountOnScreen;
    private EditText eventNameFieldOnScreen, eventDescFieldOnScreen;
    private int whichField;

    //time stuff
    private boolean newStartTimeSetFlag, newEndTimeFlag;
    private Calendar startDateTime, endDateTime, origStartDateTime, origEndDateTime, newStartDate, newEndDate;
    private int newStartYear, newStartMonth, newStartDay, newStartHour, newStartMinute, newEndYear, newEndMonth, newEndDay, newEndHour, newEndMinute;

    //final extracted fields for middle tier
    private String finalEventName, finalEventDescription, finalEventStart, finalEventEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        firstLoad = true;
        fab = (FloatingActionButton) findViewById(R.id.EventDetailsfab);

        //get info from map activity
        Bundle extras = getIntent().getExtras();
        userID = extras.getInt("userID");
        eventID = extras.getLong("eventID");
        markerInformation = (EventModel) getIntent().getSerializableExtra("markerInfo");
        currentUserInformation = (UserData) getIntent().getSerializableExtra("userInfo");

        //initialize attending to false, next line will check to see if attending
        newStartTimeSetFlag = false;         //will only be true if the user inputs a new start time
        newEndTimeFlag = false;              //will only be true if the user inputs a new end time

        //TODO://////////////////////////////////////////////////////////////////////////////////////
        //TODO::*************************************************************************************
        //TODO://////////////////////////////////////////////////////////////////////////////////////
        //attendingFlag = false;               //will only be true if the user is attending the event
        //get list of attendees
        new GetListOfAttendees().execute();  //get attendees from middle tier
        //TODO://////////////////////////////////////////////////////////////////////////////////////
        //TODO::*************************************************************************************
        //TODO://////////////////////////////////////////////////////////////////////////////////////

        //initialize calendar objects
        newStartDate = Calendar.getInstance();
        newEndDate = Calendar.getInstance();
        origStartDateTime = Calendar.getInstance();
        origEndDateTime = Calendar.getInstance();

        //convert startTime string to Calendar
        DateFormat startDT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        startDateTime = Calendar.getInstance();
        try {
            startDateTime.setTime(startDT.parse(markerInformation.startTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //convert endTime string to Calendar
        DateFormat endDT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        endDateTime  = Calendar.getInstance();
        try {
            endDateTime.setTime(endDT.parse(markerInformation.endTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Set click states based on user
        InitializeGUI();

        //declare fab button reference
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(EventDetailsActivity.this)
                        .setTitle("Edit Event")
                        .setMessage("Do you want to edit this event?")

                        //AS SOON AS YOU PRESS THIS, THE DATA IN THE FIELDS ARE EXTRACTED AND SAVED FOR MIDDLE TIER

                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                CompleteEdit();
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

        //TODO://////////////////////////////////////////////////////////////////////////////////////
        //TODO::*************************************************************************************
        //TODO://////////////////////////////////////////////////////////////////////////////////////
        //attending switch listener
        attendSwitch = (Switch) findViewById(R.id.attendingEventSwitch);
        attendSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                new UpdateAttendance().execute();   //update the attendance
                new GetListOfAttendees().execute(); //update our array with the new attendees
            }
        });
        //TODO://////////////////////////////////////////////////////////////////////////////////////
        //TODO::*************************************************************************************
        //TODO://////////////////////////////////////////////////////////////////////////////////////

        //pop up set time dialog
        startTimeFieldOnScreen = (TextView) findViewById(R.id.eStartTime);
        startTimeFieldOnScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userID == markerInformation.userID) {
                    whichField = 1;
                    showSetTimeDialog(whichField);
                }
            }
        });
        endTimeFieldOnScreen = (TextView) findViewById(R.id.eEndTime);
        endTimeFieldOnScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userID == markerInformation.userID) {
                    whichField = 2;
                    showSetTimeDialog(whichField);
                }
            }
        });

        TextView viewAttendees = (TextView) findViewById(R.id.viewAttendeesClickable);
        viewAttendees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //GOTO ATTENDING USER LIST ACTIVITY, SEND LIST OF ATTENDING USERS TO THIS ACTIVITY
                Intent startListActivity = new Intent(getApplicationContext(), AttendingUsersListActivity.class);
                startListActivity.putExtra("usersAttending", attendingUsersList);
                startActivity(startListActivity);
            }
        });
    }

    //CORRECT, NEED SMALL CHANGES
    private void InitializeGUI() {

        InitializeInitialTimes();

        System.out.println("USERID: " + userID);
        System.out.println("MARKER USERID: " + markerInformation.userID);

        //if user is event creator
        if(userID == markerInformation.userID) {
            //set Name
            eventNameFieldOnScreen = (EditText)findViewById(R.id.eNameField);
            eventNameFieldOnScreen.setText(markerInformation.eventName);

            //set Desc
            eventDescFieldOnScreen = (EditText)findViewById(R.id.eDescField);
            eventDescFieldOnScreen.setText(markerInformation.description);

            startTimeFieldOnScreen = (TextView)findViewById(R.id.eStartTime);
            startTimeFieldOnScreen.setClickable(true);

            endTimeFieldOnScreen = (TextView)findViewById(R.id.eEndTime);
            endTimeFieldOnScreen.setClickable(true);

            fab.setVisibility(View.VISIBLE);
            fab.setClickable(true);
        }
        //if user is not event creator
        else {
            fab.setVisibility(View.INVISIBLE);
            fab.setClickable(false);

            eventNameFieldOnScreen = (EditText)findViewById(R.id.eNameField);
            eventNameFieldOnScreen.setText(markerInformation.eventName);

            //set Desc
            eventDescFieldOnScreen = (EditText)findViewById(R.id.eDescField);
            eventDescFieldOnScreen.setText(markerInformation.description);

            eventNameFieldOnScreen = (EditText)findViewById(R.id.eNameField);
            eventNameFieldOnScreen.setFocusable(false);

            eventDescFieldOnScreen = (EditText)findViewById(R.id.eDescField);
            eventDescFieldOnScreen.setFocusable(false);

            startTimeFieldOnScreen = (TextView)findViewById(R.id.eStartTime);
            startTimeFieldOnScreen.setClickable(false);
            startTimeFieldOnScreen.setFocusable(false);

            endTimeFieldOnScreen = (TextView)findViewById(R.id.eEndTime);
            endTimeFieldOnScreen.setClickable(true);
            endTimeFieldOnScreen.setFocusable(false);
        }
    }

    //CORRECT
    public void InitializeInitialTimes() {

        //get references to fields
        startTimeFieldOnScreen = (TextView)findViewById(R.id.eStartTime);
        endTimeFieldOnScreen = (TextView)findViewById(R.id.eEndTime);

        ////////////////////////////////////////////////////////////////////////////////////////////
        //START TIME CONVERT------------------------------------------------------------------------
        ////////////////////////////////////////////////////////////////////////////////////////////
        //extract start time (UTC)
        int initStartTimeHour = startDateTime.get(Calendar.HOUR_OF_DAY);
        int initStartTimeMinutes = startDateTime.get(Calendar.MINUTE);
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
        int initEndTimeHour = endDateTime.get(Calendar.HOUR_OF_DAY);
        int initEndTimeMinutes = endDateTime.get(Calendar.MINUTE);
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

    public void CompleteEdit() {
        //extract input from name and description fields
        finalEventName = eventNameFieldOnScreen.getText().toString();
        finalEventDescription = eventDescFieldOnScreen.getText().toString();

        newStartDate.set(newStartYear, newStartMonth, newStartDay, newStartHour, newStartMinute);
        newEndDate.set(newEndYear, newEndMonth, newEndDay, newEndHour, newEndMinute);

        //prints out what each object currently holds, at this time DATE AND TIME OF new objects are in EST and originals are in UTC
        //The data should be correct though and is ready to be converted to UTC
        System.out.println("ORIG START DATETIME: " + origStartDateTime.get(Calendar.YEAR) + "-" + origStartDateTime.get(Calendar.MONTH) + "-" + origStartDateTime.get(Calendar.DAY_OF_MONTH) + " " + origStartDateTime.get(Calendar.HOUR_OF_DAY) + ":" + origStartDateTime.get(Calendar.MINUTE));
        System.out.println("NEW START DATETIME : " + newStartDate.get(Calendar.YEAR) + "-" + newStartDate.get(Calendar.MONTH) + "-" + newStartDate.get(Calendar.DAY_OF_MONTH) + " " + newStartDate.get(Calendar.HOUR_OF_DAY) + ":" + newStartDate.get(Calendar.MINUTE));

        System.out.println("ORIG END DATETIME: " + origEndDateTime.get(Calendar.YEAR) + "-" + origEndDateTime.get(Calendar.MONTH) + "-" + origEndDateTime.get(Calendar.DAY_OF_MONTH) + " " + origEndDateTime.get(Calendar.HOUR_OF_DAY) + ":" + origEndDateTime.get(Calendar.MINUTE));
        System.out.println("NEW END DATETIME : " + newEndDate.get(Calendar.YEAR) + "-" + newEndDate.get(Calendar.MONTH) + "-" + newEndDate.get(Calendar.DAY_OF_MONTH) + " " + newEndDate.get(Calendar.HOUR_OF_DAY) + ":" + newEndDate.get(Calendar.MINUTE));

        //only do the UTC conversions if newStartTimeSetFlags were set to true
        if(newStartTimeSetFlag) {

            //get original start datetime for comparison
            DateFormat startDT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            startDateTime = Calendar.getInstance();
            try {
                origStartDateTime.setTime(startDT.parse(markerInformation.startTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //est -> utc
            int utcStartHour = newStartHour + 5;
            //if converted hour is > 24, we need to roll it over
            if(utcStartHour >= 24)
                utcStartHour %= 24;

            //now we check for cases where we should roll days
            int originalStartHour = origStartDateTime.get(Calendar.HOUR_OF_DAY);

            ////////////////////////////////////////////////////////////////////////////////////////////
            //START CHECK-------------------------------------------------------------------------------
            ////////////////////////////////////////////////////////////////////////////////////////////
            //if newStart < originalStart : newStart day++
            if(utcStartHour < originalStartHour) {
                newStartDate.roll(Calendar.DAY_OF_MONTH, 1);
            }
            //if newStart == original BUT newStartMinute < originalStartMinute : newStart day++
            else if(utcStartHour == originalStartHour && newStartMinute < origStartDateTime.get(Calendar.MINUTE))
                newStartDate.roll(Calendar.DAY_OF_MONTH, 1);

            System.out.println("AFTER CHECKING ROLLS START:" + newStartDate);

            //correctly add 0's in front of date time fields that need them, anything < 10 needs a 0

            //START-------------------------------------------------------------------------------------

            finalEventStart = String.valueOf(newStartDate.get(Calendar.YEAR));

            if(newStartDate.get(Calendar.MONTH) < 10)
                finalEventStart = finalEventStart.concat("-0" + String.valueOf(newStartDate.get(Calendar.MONTH)));
            else
                finalEventStart = finalEventStart.concat("-" + String.valueOf(newStartDate.get(Calendar.MONTH)));

            if(newStartDate.get(Calendar.DAY_OF_MONTH) < 10)
                finalEventStart = finalEventStart.concat("-0" + String.valueOf(newStartDate.get(Calendar.DAY_OF_MONTH)));
            else
                finalEventStart = finalEventStart.concat("-" + String.valueOf(newStartDate.get(Calendar.DAY_OF_MONTH)));

            if(utcStartHour < 10)
                finalEventStart = finalEventStart.concat(" 0" + String.valueOf(utcStartHour));
            else
                finalEventStart = finalEventStart.concat(" " + String.valueOf(utcStartHour));

            if(newStartDate.get(Calendar.MINUTE) < 10)
                finalEventStart = finalEventStart.concat(":0" + String.valueOf(newStartDate.get(Calendar.MINUTE)));
            else
                finalEventStart = finalEventStart.concat(":" + String.valueOf(newStartDate.get(Calendar.MINUTE)));
        }
        else {
            finalEventStart = markerInformation.startTime;
        }

        //only do the UTC conversions if newEndTimeSetFlags were set to true
        if(newEndTimeFlag) {
            //get original end datetime for comparison
            DateFormat endDT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            endDateTime = Calendar.getInstance();
            try {
                origEndDateTime.setTime(endDT.parse(markerInformation.endTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //est->utc
            int utcEndHour = newEndHour + 5;
            if(utcEndHour >= 24)
                utcEndHour %= 24;

            //original end hour
            int originalEndHour = origEndDateTime.get(Calendar.HOUR_OF_DAY);

            ////////////////////////////////////////////////////////////////////////////////////////////
            //END CHECK---------------------------------------------------------------------------------
            ////////////////////////////////////////////////////////////////////////////////////////////
            //if newEnd < originalEnd : newEnd day++
            if(utcEndHour < originalEndHour) {
                newEndDate.roll(Calendar.DAY_OF_MONTH, 1);
            }
            //if newEnd == original BUT newEndMinute < originalEndMinute : newEnd day++
            else if(utcEndHour == originalEndHour && newEndMinute < origEndDateTime.get(Calendar.MINUTE))
                newEndDate.roll(Calendar.DAY_OF_MONTH, 1);

            //start hour for comparison with end hour
            int utcStartHour = newStartHour + 5;
            //if converted hour is > 24, we need to roll it over
            if(utcStartHour >= 24)
                utcStartHour %= 24;

            if(utcEndHour < utcStartHour)
                newEndDate.roll(Calendar.DAY_OF_MONTH, 1);


            System.out.println("AFTER CHECKING ROLLS END  :" + newEndDate);

            //END---------------------------------------------------------------------------------------

            finalEventEnd = String.valueOf(newEndDate.get(Calendar.YEAR));

            if(newEndDate.get(Calendar.MONTH) < 10)
                finalEventEnd = finalEventEnd.concat("-0" + String.valueOf(newEndDate.get(Calendar.MONTH)));
            else
                finalEventEnd = finalEventEnd.concat("-" + String.valueOf(newEndDate.get(Calendar.MONTH)));

            if(newEndDate.get(Calendar.DAY_OF_MONTH) < 10)
                finalEventEnd = finalEventEnd.concat("-0" + String.valueOf(newEndDate.get(Calendar.DAY_OF_MONTH)));
            else
                finalEventEnd = finalEventEnd.concat("-" + String.valueOf(newEndDate.get(Calendar.DAY_OF_MONTH)));

            if(utcEndHour < 10)
                finalEventEnd = finalEventEnd.concat(" 0" + String.valueOf(utcEndHour));
            else
                finalEventEnd = finalEventEnd.concat(" " + String.valueOf(utcEndHour));

            if(newEndDate.get(Calendar.MINUTE) < 10)
                finalEventEnd = finalEventEnd.concat(":0" + String.valueOf(newEndDate.get(Calendar.MINUTE)));
            else
                finalEventEnd = finalEventEnd.concat(":" + String.valueOf(newEndDate.get(Calendar.MINUTE)));
        }
        else {
            finalEventEnd = markerInformation.endTime;
        }

        System.out.println(finalEventStart);
        System.out.println(finalEventEnd);

        System.out.println("eventID: " + markerInformation.eventID);
        System.out.println("eventName: " + finalEventName);
        System.out.println("startTime: " + finalEventStart);
        System.out.println("latitude: " + markerInformation.latitude);
        System.out.println("endTime: " + finalEventEnd);
        System.out.println("longitude: " + markerInformation.longitude);
        System.out.println("description: " + finalEventDescription);

        new UpdateEventInfo().execute();
    }

    //CORRECT
    public void showSetTimeDialog(final int whichField) {
        SetTimeDialog timeDialog = SetTimeDialog.newInstance(new SetTimeDialog.SetTimeDialogListener() {
            @Override
            public void onDialogPositiveClick(int hour, int minute) {
                TextView timeView = null;
                if(whichField == 1) {

                    newStartTimeSetFlag = true;     //new end time was input

                    timeView = (TextView) findViewById(R.id.eStartTime);
                    startDateTime.set(startDateTime.get(Calendar.YEAR), startDateTime.get(Calendar.MONTH)+1, startDateTime.get(Calendar.DAY_OF_MONTH), hour, minute);
                    System.out.println("AFTER SELECTING NEW START TIME: " + startDateTime);

                    newStartYear = startDateTime.get(Calendar.YEAR);
                    newStartMonth = startDateTime.get(Calendar.MONTH);
                    newStartDay = startDateTime.get(Calendar.DAY_OF_MONTH);
                    newStartHour = startDateTime.get(Calendar.HOUR_OF_DAY);
                    newStartMinute = startDateTime.get(Calendar.MINUTE);
                }
                else if(whichField == 2) {
                    timeView = (TextView) findViewById(R.id.eEndTime);

                    newEndTimeFlag = true;      //new time was input

                    endDateTime.set(endDateTime.get(Calendar.YEAR), endDateTime.get(Calendar.MONTH)+1, endDateTime.get(Calendar.DAY_OF_MONTH), hour, minute);
                    System.out.println("AFTER SELECTING NEW END TIME: " + endDateTime);

                    newEndYear = endDateTime.get(Calendar.YEAR);
                    newEndMonth = endDateTime.get(Calendar.MONTH);
                    newEndDay = endDateTime.get(Calendar.DAY_OF_MONTH);
                    newEndHour = endDateTime.get(Calendar.HOUR_OF_DAY);
                    newEndMinute = endDateTime.get(Calendar.MINUTE);
                }

                //FOR SCREEN DISPLAY PURPOSE ONLY-------------------------------------------------------
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

    public class UpdateEventInfo extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {

            JSONObject updatedInformation = new JSONObject();
            try {
                updatedInformation.put("eventID", markerInformation.eventID);
                updatedInformation.put("eventName", finalEventName);
                updatedInformation.put("startTime", finalEventStart);
                updatedInformation.put("latitude", markerInformation.longitude);
                updatedInformation.put("endTime", finalEventEnd);
                updatedInformation.put("longitude", markerInformation.latitude);
                updatedInformation.put("description", finalEventDescription);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println("JSON OBJECT: " + updatedInformation);

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("event", updatedInformation.toString())
                    .build();

            Request request = new Request.Builder()
                    .url("http://162.243.15.139/editevent_alt")
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                if(Objects.equals(response.body().string(), "1")) {
                    System.out.println("EDITED");
                }
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent gotoMapActivity = new Intent(getApplicationContext(), MapActivity.class);
            gotoMapActivity.putExtra("UserData", currentUserInformation);
            setResult(2, gotoMapActivity);
            finish();
        }
    }

    //TODO://////////////////////////////////////////////////////////////////////////////////////
    //TODO::*************************************************************************************
    //TODO://////////////////////////////////////////////////////////////////////////////////////
    public class UpdateAttendance extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            System.out.println("In updating attendance");

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("userID", String.valueOf(userID))
                    .add("eventID", String.valueOf(eventID))
                    .build();

            Request request = new Request.Builder()
                    .url("http://162.243.15.139/attend")
                    .post(formBody)
                    .build();

            try {
                //try to get a response
                Response response= client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                //store the response [NOTE: DO NOT USE response.body() BEFORE THIS LINE BECAUSE IT WILL CONSUME THE RETURN]
                attendanceResponse = response.body().string();
                response.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            System.out.println("In updating attendance post view");
        }
    }

    //CORRECT
    private class GetListOfAttendees extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            System.out.println("Getting attendees!");

            OkHttpClient client = new OkHttpClient();

            HttpUrl url = HttpUrl.parse("http://162.243.15.139/getattendance");
            HttpUrl.Builder myBuilder = url.newBuilder();
            myBuilder.addQueryParameter("eventID", String.valueOf(eventID));

            Request request = new Request.Builder()
                    .url(myBuilder.build())
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                allAttendingUsers = response.body().string();

                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            System.out.println("Done getting attendees!");

            attendingUsersList = null;
            attendingUsersList = UserData.FromJsonArray(allAttendingUsers);

            for(int i = 0; i < attendingUsersList.size(); i++) {
                System.out.println("Current Attendee ID: " + attendingUsersList.get(i).userID);
                System.out.println("Current USER ID: " + currentUserInformation.userID);

                if(attendingUsersList.get(i).userID == currentUserInformation.userID) {
                    attendingFlag = true;
                    System.out.println("EQUAL");
                    break;
                }
                else
                    attendingFlag = false;
            }

            if(firstLoad) {
                if (attendingFlag == true) {
                    System.out.println("Setting switch to true");

                    attendSwitch = (Switch) findViewById(R.id.attendingEventSwitch);
                    attendSwitch.setChecked(true);
                } else if (attendingFlag == false) {
                    System.out.println("Setting switch to false");

                    attendSwitch = (Switch) findViewById(R.id.attendingEventSwitch);
                    attendSwitch.setChecked(false);
                }
                firstLoad = false;
            }

            attendanceCount = attendingUsersList.size();
            attendanceCountOnScreen = (TextView)findViewById(R.id.attendDisplay);
            attendanceCountOnScreen.setText(String.valueOf(attendanceCount));

            System.out.println("Done with post execute get attendees");
        }
    }
    //TODO://////////////////////////////////////////////////////////////////////////////////////
    //TODO::*************************************************************************************
    //TODO://////////////////////////////////////////////////////////////////////////////////////
}
