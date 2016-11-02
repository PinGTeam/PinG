package com.example.jorge.pingv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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



public class SignupActivity extends AppCompatActivity {

    Button signUpButton;
    String uName, uFname, uLname, uEmail, uPass, uConfPass, middleTierResponse, encodedPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signUpButton = (Button) findViewById(R.id.button2);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //store user data
                EditText usernameField = (EditText) findViewById(R.id.userameInput);
                uName = usernameField.getText().toString();

                EditText firstName = (EditText) findViewById(R.id.fNameInput);
                uFname = firstName.getText().toString();

                EditText lastName = (EditText) findViewById(R.id.lNameInput);
                uLname = lastName.getText().toString();

                EditText email = (EditText) findViewById(R.id.emailInput);
                uEmail = email.getText().toString();

                EditText password = (EditText) findViewById(R.id.passInput);
                uPass = password.getText().toString();

                EditText confPassword = (EditText) findViewById(R.id.confPassInput);
                uConfPass = confPassword.getText().toString();

                //if passwords are equal
                if(Objects.equals(uPass, uConfPass)) {
                    try {
                        byte[] enPass = Base64.encode(uPass.getBytes("UTF-8"), Base64.DEFAULT);
                        encodedPass = new String(enPass);

                        System.out.println("encodedPASS SignUp: " + encodedPass);

                        new SendSignUpData().execute();

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                //else error message
                else {
                    Toast.makeText(SignupActivity.this, "Passwords don't match. Try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private class SendSignUpData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("userName", uName)
                    .add("firstName", uFname)
                    .add("lastName", uLname)
                    .add("password", encodedPass)
                    .add("email", uEmail)
                    .build();

            Request request = new Request.Builder()
                    .url("http://162.243.15.139/adduser")
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

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
            String positive = "1";
            if (Objects.equals(middleTierResponse, positive)) {
                Toast.makeText(SignupActivity.this, "User created. You may now sign in", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LogIn.class);
                startActivity(intent);
            } else {
                switch (middleTierResponse) {
                    case "-1":
                        Toast.makeText(SignupActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                        break;
                    case "-2":
                        Toast.makeText(SignupActivity.this, "User name already exists", Toast.LENGTH_SHORT).show();
                        break;
                    case "-3":
                        Toast.makeText(SignupActivity.this, "User name and email exist", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
