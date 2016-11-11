package com.example.jorge.pingv2;

import android.app.Dialog;
import android.widget.Toast;

import com.example.jorge.pingv2.MapActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.regex.Pattern;
import org.junit.runner.Request;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Originally introduced by Arthur and Jorge on 10/19/2016.
 * Ammended and made right by Jorge, Zach, and Richard on 11/08/2016.
 **/

public class UnitTests {
    @Before
    public void setUp() throws Exception {
        //setUp();
        //this function is called before the invocation of
        //each test method in the class.
    }

    @After
    public void tearDown() throws Exception {
        //tearDown();
        //this function is called after the invocation of
        //each test method in the class.
    }

    // ---------------- tests that input is not empty or null -----------------------
    @Test
    public void stringValidator() {
        assertThat(testInput("firstname", "lastname", "userId"), is(true));
    }

    private boolean testInput(String firstname, String lastname, String userId) {

        if (firstname != null && !firstname.isEmpty()) {
            if (lastname != null && !lastname.isEmpty()) {
                if (userId != null && !userId.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    String serverResponse;

    @Test
    public void testMiddletierLogin() {

        //connect to mid-tier
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("userName", "userName1")
                .add("password", "dGhlcGFzc3dvcmQ=")
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("http://162.243.15.139/login")
                .post(formBody)
                .build();

        //get response
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            //store the response [NOTE: DO NOT USE response.body() BEFORE THIS LINE BECAUSE IT WILL CONSUME THE RETURN]
            serverResponse = response.body().string();
            assertThat(!Objects.equals(serverResponse, "-1"), is(true));
            response.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMiddletierSetup() {

        //connect to mid-tier
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("userName", "userName" + System.currentTimeMillis())
                .add("firstName", "firstname")
                .add("lastName", "lastname")
                .add("password", "dGhlcGFzc3dvcmQ=")
                .add("email", "email@validemail.com")
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("http://162.243.15.139/adduser")
                .post(formBody)
                .build();

        //get response
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            //store the response [NOTE: DO NOT USE response.body() BEFORE THIS LINE BECAUSE IT WILL CONSUME THE RETURN]
            serverResponse = response.body().string();
            assertThat(Objects.equals(serverResponse, "1"), is(true));
            response.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEventModel()
    {
        EventModel model = new EventModel();
        model.endTime = "2016-10-10 16:00:05";
        model.startTime = "2016-10-10 15:30:05";

        model.eventName = "Hello";
        model.description="Mine";
        model.latitude = -10.005;
        model.longitude =  140.93838;

        model.userID = 1;

        RequestBody postData = model.getPostFormData();

    }

    /*
    @Test
    protected UnitTests(){
        //create json object and stitch all data together
        JSONObject geometry = new JSONObject();
        try {
            JSONArray coord = new JSONArray("[" + theCoords.longitude + ", " + theCoords.latitude + "]");
            //need to get rid of the "" around coordinates

            geometry.put("coordinates", coord);
            geometry.put("type", "Point");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject properties = new JSONObject();
        try {
            properties.put("description", eDescription);
            properties.put("eventName", eName);
            properties.put("time", eTime);
            properties.put("userID", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject geoJSON = new JSONObject();
        try {
            geoJSON.put("geometry", geometry);
            geoJSON.put("properties", properties);
            geoJSON.put("type", "Feature");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    //Check Google Play Services Availability
    private boolean checkGoogleServices() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);

        //is available
        if (isAvailable == ConnectionResult.SUCCESS)
            return true;
            //not available, but can install
        else if (api.isUserResolvableError(isAvailable)) {
            Dialog box = api.getErrorDialog(this, isAvailable, 0);
            box.show();
            //is not available
        } else {
            Toast.makeText(this, "Cannot connect to Google Play Services", Toast.LENGTH_LONG).show();
        }
        return false;
    }
*/
}
