package com.example.jorge.pingv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LogIn extends AppCompatActivity {

    String userCode;   //stores userID sent back from middle tier
    Button logIn;

    String Fid, Fname, Flast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //create button on click listener
        logIn = (Button) findViewById(R.id.logInButton);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //store user data
                EditText id = (EditText) findViewById(R.id.idInput);
                String userID = id.getText().toString();

                EditText fname = (EditText) findViewById(R.id.idInput);
                String userfName = fname.getText().toString();

                EditText lname = (EditText) findViewById(R.id.idInput);
                String userlName = lname.getText().toString();

                //user input to be passed to mid-tier
                Fid = userID;
                Fname = userfName;
                Flast = userlName;

                //perform async task
                new SendUserData().execute();
            }
        });
    }

    private class SendUserData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("UserName", Fid)
                    .add("Name", Fname)
                    .add("LName", Flast)
                    .build();
            Request request = new Request.Builder()
                    .url("http://162.243.15.139/adduser")
                    .post(formBody)
                    .build();

                try {
                    Response response= client.newCall(request).execute();
                    if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    //store the response [NOTE: DO NOT USE response.body() BEFORE THIS LINE BECAUSE IT WILL CONSUME THE RETURN]
                    userCode = response.body().string();
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            return null;
        }

        //once the middle-tier sends us back the response, go to map and send user code with it
        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            intent.putExtra("key", userCode);
            startActivity(intent);
        }
    }
}