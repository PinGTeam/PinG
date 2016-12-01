/**
 * Ammended and made right by Zach and Richard and Jorge Torres-Aldana*
 **/

package com.example.jorge.pingv2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class AttendingUsersListActivity extends AppCompatActivity {

    private TextView attendingEvent;
    private ListView listOfAttending;
    private ArrayList<UserData> attendeeList;
    private ArrayList<String> attendeeNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attending_users_list);

        //get users from event details
        attendeeList = (ArrayList<UserData>) getIntent().getSerializableExtra("usersAttending");
        attendeeNames = new ArrayList<String>();

        for(int i = 0; i < attendeeList.size(); i++) {
            attendeeNames.add(attendeeList.get(i).firstName + " " + attendeeList.get(i).lastName);
        }

        //set attendance number
        attendingEvent = (TextView)findViewById(R.id.attendingCountField);
        attendingEvent.setText(String.valueOf(attendeeList.size()));

        //
        listOfAttending = (ListView)findViewById(R.id.listOfAttendeesOnScreen);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.custom_textview,
                attendeeNames);

        listOfAttending.setAdapter(arrayAdapter);
    }
}
