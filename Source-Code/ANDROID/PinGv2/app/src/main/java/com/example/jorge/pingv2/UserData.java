/**
 * Ammended and made right by Zach and Richard and Jorge Torres-Aldana*
 **/

package com.example.jorge.pingv2;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class UserData implements Serializable{

    public int userID;
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

    public static ArrayList<UserData> FromJsonArray(String jsonText)
    {
        Type arrayType = new TypeToken<ArrayList<UserData>>() {}.getType();

        Gson gs = new Gson();
        ArrayList<UserData> userList = gs.fromJson(jsonText, arrayType);

        return userList;
    }
}
