/**
 * Ammended and made right by Zach and Richard and Jorge Torres-Aldana*
 **/

package com.example.jorge.pingv2;

import android.app.Dialog;
import android.widget.Toast;

import com.example.jorge.pingv2.MapActivity;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThat;

import java.util.Objects;
import java.util.regex.Pattern;
//import org.junit.runner.Request;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Originally introduced by Arthur and Jorge on 10/19/2016. *
 * Ammended and made right by Zach and Richard *
 **/

public class UnitTests {
    @Before
    public void setUp() throws Exception {
        //setUp(); not needed
        //this function is called before the invocation of
        //each test method in the class.
    }

    @After
    public void tearDown() throws Exception {
        //tearDown(); not needed
        //this function is called after the invocation of
        //each test method in the class.
    }

    // ---------------- tests post body -----------------------
    @Test
    public void stringValidator() {
        assertThat(testInput("firstname", "lastname", "userId"), is(true));
    }

    private boolean testInput(String firstname, String lastname, String userId) {

        if (firstname != null && !firstname.isEmpty()) {
            if (lastname != null && !lastname.isEmpty()) {
                if (userId != null && !userId.isEmpty()) {
                    System.out.println("Unit test 1 passed. (Tests post body)");
                    return true;
                }
            }
        }
        System.out.println("Unit test 1 failed. (Tests post body)");
        return false;
    }

    String serverResponse;

    // tests login with existing user
    @Test
    public void testMiddletierLogin() {

        //connect to mid-tier
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("userName", "userName1")
                .add("password", "dGhlcGFzc3dvcmQ=")
                .build();
        Request request = new Request.Builder()
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
            if(!Objects.equals(serverResponse, "-1"))
                System.out.println("Unit test 2 passed. (Tests existing user login)");
            else
                System.out.println("Unit test 2 failed. (Tests existing user login)");

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
                .add("userName", "userName" + System.currentTimeMillis()%99999)
                .add("firstName", "firstname")
                .add("lastName", "lastname")
                .add("password", "dGhlcGFzc3dvcmQ=")
                .add("email", System.currentTimeMillis()%99999 + "email@validemail.com")
                .build();


        Request request = new Request.Builder()
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
            if(Objects.equals(serverResponse, "1"))
                System.out.println("Unit test 3 passed. (Tests new user signup)");
            else
                System.out.println("Unit test 3 failed. (Tests new user signup)");
            response.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // tests creating eventmodel class for storing event data
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


    // test creating an event
    LatLng theCoords = new LatLng(30.4059723, -84.21904);
    String eName = "EventName" + System.currentTimeMillis()%99999;
    String eStartTime = "2016-12-29 16:37:00";
    String eEndTime = "2016-12-29 19:37:00";
    String eDescription = "Event test description";
    String uID = "1";

    @Test
    public void testAddEvent() {
        EventModel model = new EventModel();
        model.endTime = "2016-10-10 16:00:05";
        model.startTime = "2016-10-10 15:30:05";
        model.eventName = "Hello";
        model.description="Mine";
        model.latitude = -10.005;
        model.longitude =  140.93838;
        model.userID = 1;
        RequestBody formBody = model.getPostFormData();

        OkHttpClient client = new OkHttpClient();
        /*
        RequestBody formBody = new FormBody.Builder()
                .add("event", markerInfo.toString())
                .build();
         */


        Request request = new Request.Builder()
                .url("http://162.243.15.139/addevent_alt")
                .post(formBody)
                .build();
        try {
            // retrieve response
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();

            // assert that response is valid
            assertThat(Objects.equals(responseBody, "1"), is(true));
            if(Objects.equals(responseBody, "1")) {
                System.out.println("Unit test 4 passed. (Tests new event creation)");
            }
            else
                System.out.println("Unit test 4 failed. (Tests new event creation)");
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}