/**
 * Ammended and made right by Zach and Richard and Jorge Torres-Aldana*
 **/

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

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.jorge.pingv2.R.drawable.edit_btn;
import static java.lang.Math.abs;


//TODO: CHANGE THIS TO MATCH THE EVENT DETAIL
public class EventDetailsActivity extends AppCompatActivity {

    //variables holding information about the event and the user
    private int userID;
    private long eventID;
    private EventModel markerInformation;
    private UserData currentUserInformation;

    //flag to signal first load of the screen
    private boolean firstLoad;

    //users attending stuff
    private Switch attendSwitch;                    //the actual switch itself
    private boolean attendingFlag;                  //set true if the user is attending
    private int attendanceCount;                    //how many attendees
    private String attendanceResponse;              //server response
    private String allAttendingUsers;               //JSON of attendees
    private ArrayList<UserData> attendingUsersList; //Array of attendees

    //GIU stuff
    private FloatingActionButton fab;
    private TextView startTimeFieldOnScreen, endTimeFieldOnScreen, attendanceCountOnScreen;
    private EditText eventNameFieldOnScreen, eventDescFieldOnScreen;
    private int whichField;

    //time stuff
    private boolean newStartTimeSetFlag, newEndTimeFlag;
    private int newStartHour, newStartMinute, newEndHour, newEndMinute;
    private Calendar originalStartDateTime, originalEndDateTime, newStartDateTime, newEndDateTime;

    private String convertedFinalEventEnd;

    private boolean useThisFlag;
    private int globalUTCSTART;


    //final extracted fields for server
    private String finalEventName, finalEventDescription, finalEventStart, finalEventEnd;

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
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

        //initialize time changed flags
        newStartTimeSetFlag = false;         //will only be true if the user inputs a new start time
        newEndTimeFlag = false;              //will only be true if the user inputs a new end time

        //get list of attendees, set attend switch position and display number of attendees showing
        new GetListOfAttendees().execute();  //get attendees from middle tier

        //set gregorian start and end calendars converted from strings
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date parsedS = sdf.parse(markerInformation.startTime);
            originalStartDateTime = new GregorianCalendar();
            originalStartDateTime.setTime(parsedS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateFormat edf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date parsedE = edf.parse(markerInformation.endTime);
            originalEndDateTime = new GregorianCalendar();
            originalEndDateTime.setTime(parsedE);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //set new start and new end to original
        newStartDateTime = new GregorianCalendar();
        newStartDateTime = originalStartDateTime;

        newEndDateTime = new GregorianCalendar();
        newEndDateTime = originalEndDateTime;

        //set to correct month because java's month start at 0 for some reason...
        originalStartDateTime.add(Calendar.MONTH, 1);
        originalEndDateTime.add(Calendar.MONTH, 1);

        System.out.println("GREGY S: " + originalStartDateTime);
        System.out.println("GREGY E: " + originalEndDateTime);

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

        //attending switch listener
        attendSwitch = (Switch) findViewById(R.id.attendingEventSwitch);
        attendSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                new UpdateAttendance().execute();   //update the attendance
                new GetListOfAttendees().execute(); //update our array with the new attendees
            }
        });

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

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
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

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
    public void InitializeInitialTimes() {

        //get references to fields
        startTimeFieldOnScreen = (TextView)findViewById(R.id.eStartTime);
        endTimeFieldOnScreen = (TextView)findViewById(R.id.eEndTime);

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
    public void CompleteEdit() {

        useThisFlag = false;

        eventNameFieldOnScreen = (EditText)findViewById(R.id.eNameField);
        eventDescFieldOnScreen = (EditText)findViewById(R.id.eDescField);

        //extract input from name and description fields
        finalEventName = eventNameFieldOnScreen.getText().toString();
        finalEventDescription = eventDescFieldOnScreen.getText().toString();

        //only do the UTC conversions if newStartTimeSetFlags were set to true
        if(newStartTimeSetFlag) {

            //est -> utc
            int utcStartHour = newStartHour + 5;
            globalUTCSTART = utcStartHour;
            //if converted hour is > 24, we need to roll it over
            if(utcStartHour >= 24)
                utcStartHour %= 24;

            //now we check for cases where we should roll days
            int originalStartHour = originalStartDateTime.get(Calendar.HOUR_OF_DAY);

            ////////////////////////////////////////////////////////////////////////////////////////////
            //START CHECK-------------------------------------------------------------------------------
            ////////////////////////////////////////////////////////////////////////////////////////////

            //if newStart < originalStart : newStart day++
            if(utcStartHour < originalStartHour) {
                newStartDateTime.add(Calendar.DAY_OF_MONTH, 1);
            }
            //if newStart == original BUT newStartMinute < originalStartMinute : newStart day++
            else if(utcStartHour == originalStartHour && newStartMinute < originalStartDateTime.get(Calendar.MINUTE))
                newStartDateTime.add(Calendar.DAY_OF_MONTH, 1);

            System.out.println("After rolling, the startDate is: " + newStartDateTime);

            //convert start calendar to string
            finalEventStart = String.valueOf(newStartDateTime.get(Calendar.YEAR));

            if(newStartDateTime.get(Calendar.MONTH) < 10) {
                finalEventStart = finalEventStart.concat("-0" + String.valueOf(newStartDateTime.get(Calendar.MONTH)));
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

            System.out.println("After rolling, the endDate is: " + newEndDateTime);


            //convert end calendar to string
            finalEventEnd = String.valueOf(newEndDateTime.get(Calendar.YEAR));

            if(newEndDateTime.get(Calendar.MONTH) < 10)
                finalEventEnd = finalEventEnd.concat("-0" + String.valueOf(newEndDateTime.get(Calendar.MONTH)));
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

            System.out.println("END: " + originalEndDateTime.get(Calendar.HOUR_OF_DAY));
            System.out.println("START: " + newStartDateTime.get(Calendar.HOUR_OF_DAY));

            System.out.println(originalEndDateTime);

            //if we set a new start time
            if(newStartTimeSetFlag) {
                //if original end is less than new start, original end day ++
                if (originalEndDateTime.get(Calendar.HOUR_OF_DAY) < globalUTCSTART) {
                    System.out.println("WE HERE");

                    //IF WE ARE HERE< THEN ORIG END TIME HAS CHANGED SO WE NEED TO SEND A SIGNAL TO THE STUFF BELOW SAYING SO

                    originalEndDateTime.add(Calendar.DAY_OF_MONTH, 2);
                    originalEndDateTime.add(Calendar.HOUR_OF_DAY, 5);
                    System.out.println(originalEndDateTime);

                    convertedFinalEventEnd = endForm.format(originalEndDateTime.getTime());
                    useThisFlag = true;

                    newEndTimeFlag = true;
                }
            }
        }

        System.out.println("eventName: " + finalEventName);
        System.out.println("startTime: " + finalEventStart);
        System.out.println("latitude: " + markerInformation.latitude);
        System.out.println("endTime: " + finalEventEnd);
        System.out.println("userID: " + currentUserInformation.userID);
        System.out.println("longitude: " + markerInformation.longitude);
        System.out.println("description: " + finalEventDescription);

        //SET THE CORRECT MONTHS TO THE STAR DATE
        String delim = "-";                 //cut the string into bits
        String correctedSMonth = "";
        String FINALfinalStart = finalEventStart;

        if(newStartTimeSetFlag) {
            FINALfinalStart = "";
            String[] tokens = finalEventStart.split(delim);
            for (int i = 0; i < tokens.length; i++) {
                System.out.println("Split Output: " + tokens[i]);
                System.out.println("");
                if (i == 1) {
                    int currMonth = Integer.parseInt(tokens[i]);        //TAKE THE CURRENT MONTH, 1 OFF
                    currMonth++;                                        //ADD ONE
                    correctedSMonth = String.valueOf(currMonth);        //THIS NOW HAS THE CORRECT TIME
                    FINALfinalStart = FINALfinalStart.concat(correctedSMonth + "-");
                } else {
                    if (i != 2) {
                        FINALfinalStart = FINALfinalStart.concat(tokens[i]) + "-";
                    } else
                        FINALfinalStart = FINALfinalStart.concat(tokens[i]);
                }
            }
        }

        String FINALfinalEnd = finalEventEnd;
        String correctedEMonth;

        if(newEndTimeFlag) {
            FINALfinalEnd = "";
            String[] endTokens = null;
            if(useThisFlag) {
                endTokens = convertedFinalEventEnd.split(delim);
            }
            else
                endTokens = finalEventEnd.split(delim);

            for (int i = 0; i < endTokens.length; i++) {
                System.out.println("Split Output: " + endTokens[i]);
                System.out.println("");
                if (i == 1) {
                    if(!(Objects.equals(endTokens[i], "12"))) {
                        int currMonth = Integer.parseInt(endTokens[i]);      //TAKE THE CURRENT MONTH, 1 OFF
                        currMonth++;                                        //ADD ONE
                        correctedEMonth = String.valueOf(currMonth);        //THIS NOW HAS THE CORRECT TIME
                        FINALfinalEnd = FINALfinalEnd.concat(correctedEMonth + "-");
                    }
                    else {
                        int currMonth = Integer.parseInt(endTokens[i]);
                        FINALfinalEnd = FINALfinalEnd.concat(String.valueOf(currMonth) + "-");
                    }
                } else {
                    if (i != 2) {
                        FINALfinalEnd = FINALfinalEnd.concat(endTokens[i]) + "-";
                    } else
                        FINALfinalEnd = FINALfinalEnd.concat(endTokens[i]);
                }
            }
        }

        System.out.println("NOW THIS IS THE FINAL EVENT START: " + FINALfinalStart);
        System.out.println("NOW THIS IS THE FINAL EVENT END: " + FINALfinalEnd);

        finalEventStart = FINALfinalStart;
        finalEventEnd = FINALfinalEnd;

        //NOW THAT WE HAVE THE

        //call the server to post
        new UpdateEventInfo().execute();
    }

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
    public void showSetTimeDialog(final int whichField) {
        SetTimeDialog timeDialog = SetTimeDialog.newInstance(new SetTimeDialog.SetTimeDialogListener() {
            @Override
            public void onDialogPositiveClick(int hour, int minute) {

                TextView timeView = null;

                if(whichField == 1) {

                    newStartTimeSetFlag = true;     //new end time was input
                    timeView = (TextView) findViewById(R.id.eStartTime); //select field to print time to

                    //set the new startDateTime to the newly selected start time with original start year, month, and day
                    newStartDateTime.set(originalStartDateTime.get(Calendar.YEAR), originalStartDateTime.get(Calendar.MONTH), originalStartDateTime.get(Calendar.DAY_OF_MONTH), hour, minute, 00);
                    System.out.println("AFTER SELECTING NEW START TIME: " + newStartDateTime);

                    //save the values for later use
                    newStartHour = newStartDateTime.get(Calendar.HOUR_OF_DAY);
                    newStartMinute = newStartDateTime.get(Calendar.MINUTE);
                }
                else if(whichField == 2) {

                    newEndTimeFlag = true;      //new time was input
                    timeView = (TextView) findViewById(R.id.eEndTime);  //select field to print time to

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

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
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

    //THIS FUNCTION IS CORRECT AT THIS POINT : 11/30/2016 2:30am
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
}
