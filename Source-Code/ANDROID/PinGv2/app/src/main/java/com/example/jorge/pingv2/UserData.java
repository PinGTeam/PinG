package com.example.jorge.pingv2;

import android.util.Base64;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by Richard Hamm on 11/3/2016.
 */

public class UserData {

    public int userId;
    public String userName;
    public String firstName;
    public String lastName;
    public String email;

    public static UserData FromJson(String jsonText)
    {
        Gson gs = new Gson();
        UserData data = gs.fromJson(jsonText, UserData.class);

        return data;
    }

    public RequestBody GetLoginPostData(String password) {
        String encodedPass = "";
        try {
            byte[] enPass = Base64.encode(password.getBytes("UTF-8"), Base64.DEFAULT);
            encodedPass = new String(enPass);

            //delete tailing newline
            encodedPass = encodedPass.replaceAll(System.getProperty("line.separator"), "");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        RequestBody formBody = new FormBody.Builder()
                .add("userName", userName)
                .add("password", encodedPass)
                .build();

        return formBody;
    }

    public RequestBody GetSignUpPostData(String password){
        String encodedPass = "";

        try {
            byte[] enPass = Base64.encode(password.getBytes("UTF-8"), Base64.DEFAULT);
            encodedPass = new String(enPass);

            //delete tailing newline
            encodedPass = encodedPass.replaceAll(System.getProperty("line.separator"), "");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        RequestBody formBody = new FormBody.Builder()
                .add("userName", userName)
                .add("password", encodedPass)
                .add("email", email)
                .add("firstName", firstName)
                .add("lastName", lastName)
                .build();

        return formBody;




    }

}
