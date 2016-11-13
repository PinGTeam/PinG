package com.example.jorge.pingv2;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class CreateEventActivity extends AppCompatActivity {

    private TextView eventStartInput, eventEndInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        eventStartInput = (TextView)findViewById(R.id.eventStartField);
        eventStartInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Start Clicked", Toast.LENGTH_SHORT).show();
                /*
                final Dialog startTimeDialog = new Dialog(getApplicationContext());
                startTimeDialog.setContentView(R.layout.startDialog);
                startTimeDialog.setTitle("Select Start Time");

                //spinner stuff

                startTimeDialog.show();
                */
            }
        });

        eventEndInput = (TextView)findViewById(R.id.eventEndField);
        eventEndInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "End Clicked", Toast.LENGTH_SHORT).show();
                /*
                final Dialog endTimeDialog = new Dialog(getApplicationContext());
                endTimeDialog.setContentView(R.layout.endDialog);
                endTimeDialog.setTitle("Select End Time");

                //spinner stuff

                endTimeDialog.show();
                */
            }
        });
    }
}