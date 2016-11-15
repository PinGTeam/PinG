package com.example.jorge.pingv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
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

public class SignUpActivity extends AppCompatActivity {

    private boolean check;
    private Button signUpButton;
    private String uName, uFname, uLname, uEmail, uPass, uConfPass, middleTierResponse, encodedPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpButton = (Button) findViewById(R.id.createUser);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                check = true;

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

                if(uName.length() < 1) {
                    check = false;
                    Toast.makeText(getApplicationContext(), "User Name too short. Try again", Toast.LENGTH_SHORT).show();
                }
                else if(uFname.length() < 1) {
                    check = false;
                    Toast.makeText(getApplicationContext(), "First name too short. Try again", Toast.LENGTH_SHORT).show();
                }
                else if(uLname.length() < 1) {
                    check = false;
                    Toast.makeText(getApplicationContext(), "Last name too short. Try again", Toast.LENGTH_SHORT).show();
                }
                else if(!isValidEmail(uEmail)) {
                    check = false;
                    Toast.makeText(getApplicationContext(), "Invalid email. Try again", Toast.LENGTH_SHORT).show();
                }
                else if(uPass.length() < 1) {
                    check = false;
                    Toast.makeText(getApplicationContext(), "Password too short. Try again", Toast.LENGTH_SHORT).show();
                }
                else if(uConfPass.length() < 1) {
                    check = false;
                    Toast.makeText(getApplicationContext(), "Confirm Password too short. Try again", Toast.LENGTH_SHORT).show();
                }

                if(check == true) {
                    //if passwords are equal
                    if (Objects.equals(uPass, uConfPass)) {
                        try {
                            byte[] enPass = Base64.encode(uPass.getBytes("UTF-8"), Base64.DEFAULT);
                            encodedPass = new String(enPass);
                            //delete trailing newline
                            encodedPass = encodedPass.replaceAll(System.getProperty("line.separator"), "");

                            new SendSignUpData().execute();

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    //else error message
                    else {
                        Toast.makeText(SignUpActivity.this, "Passwords don't match. Try again", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
                Toast.makeText(SignUpActivity.this, "User created. You may now sign in", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                startActivity(intent);
            } else {
                switch (middleTierResponse) {
                    case "-1":
                        Toast.makeText(SignUpActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                        break;
                    case "-2":
                        Toast.makeText(SignUpActivity.this, "User name already exists", Toast.LENGTH_SHORT).show();
                        break;
                    case "-3":
                        Toast.makeText(SignUpActivity.this, "User name and email exist", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
