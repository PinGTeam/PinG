package com.example.jorge.pingv2;

import android.util.JsonToken;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by Richard Hamm on 11/10/2016.
 */

public class EventModel {
    public long eventID;
    public long userID;
    public String description;
    public String eventName;
    public double latitude;
    public double longitude;

    public String startTime;
    public String endTime;

    public RequestBody getPostFormData() {
        Gson gs = new Gson();
        String data = gs.toJson(this);

        RequestBody formBody = new FormBody.Builder()
                .add("event", data)
                .build();

        return formBody;

    }

    public static EventModel fromJson(String jsonText) {
        Gson gs = new Gson();
        EventModel data = gs.fromJson(jsonText, EventModel.class);

        return data;
    }

    public static ArrayList<EventModel> fromArrayJson(String jsonArray) {
        Type arrayType = new TypeToken<ArrayList<EventModel>>() {
        }.getType();

        Gson gs = new Gson();
        ArrayList<EventModel> eventList = gs.fromJson(jsonArray, arrayType);

        return eventList;
    }
}
