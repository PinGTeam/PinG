package com.example.jorge.pingv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LogIn extends AppCompatActivity {

    Button logIn, signUp;
    private String userName, userPass, encodedPass, middleTierResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //login button press
        logIn = (Button) findViewById(R.id.logInButton);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //store user data
                EditText usernameField = (EditText) findViewById(R.id.idInput);
                userName = usernameField.getText().toString();

                EditText passwordFiled = (EditText) findViewById(R.id.passInput);
                userPass = passwordFiled.getText().toString();
                //add checks for empty logIn input

                new SendUserData().execute();
            }
        });

        //signup button press
        signUp = (Button) findViewById(R.id.signUpButton);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to signup create screen
                Intent signuptest = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(signuptest);
            }
        });
    }

    private class SendUserData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            //create connection client
            OkHttpClient client = new OkHttpClient();

            UserData user = new UserData();
            user.userName = userName;

            RequestBody formBody = user.GetLoginPostData(userPass);

            //post that JSON object
            Request request = new Request.Builder()
                    .url("http://162.243.15.139/login")
                    .post(formBody)
                    .build();

            try {
                //try to get a response
                Response response= client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                //store the response [NOTE: DO NOT USE response.body() BEFORE THIS LINE BECAUSE IT WILL CONSUME THE RETURN]
                middleTierResponse = response.body().string();
                response.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        //once the middle-tier sends us back the response, go to map and send user code with it
        @Override
        protected void onPostExecute(Void aVoid) {
            String failedRes = "-1";
            if(Objects.equals(middleTierResponse, failedRes)) {
                Toast.makeText(LogIn.this, "Log in failed, retry or sign up instead", Toast.LENGTH_SHORT).show();
            }
            //if successful go to map activity
            else {
                UserData user = UserData.FromJson(middleTierResponse);
                Intent mapActivityStart = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(mapActivityStart);

                // TODO: Send user data to map activity class
            }
        }
    }
}