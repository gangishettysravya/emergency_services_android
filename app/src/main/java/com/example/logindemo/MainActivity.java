package com.example.logindemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

//Created by Manisha

public class MainActivity extends AppCompatActivity {

    SessionUtil session;
    private EditText User_Name;
    private EditText Password;
    private Button Login;
    private TextView userRegistration;
    private String idToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        User_Name = (EditText) findViewById(R.id.etName);
        Password = (EditText) findViewById(R.id.etPassword);
        Login = (Button) findViewById(R.id.btnLogin);
        userRegistration = (TextView) findViewById(R.id.tvRegister);

        session = new SessionUtil(getApplicationContext());

        //GoogleApiAvailability.makeGooglePlayServicesAvailable(); TODO

            /*
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d("Main Activity", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                //        String msg = getString(R.string.msg_token_fmt, token);
                //        Log.d(TAG, msg);
                //        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.d("Token recieved",token);
                    }
                });
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken = task.getResult().getToken();

                            if(session.getToken()!=null && session.getToken().equals(idToken)){
                                // TODO SEND IT TO THE BACKEND.
                            }

                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });
        */

        checkLogin();

        userRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(User_Name.getText().toString(), Password.getText().toString());

            }

        });

    }

    private void checkLogin() {

        if (session.isLoggedIn()) {

            String userType = session.getUserCategory();

            if (userType.equals("Citizen")) {
                /*
                Intent intent = new Intent(MainActivity.this, CitizenHomeActivity.class);
                startActivity(intent);
                finish();
                */
                Intent intent = new Intent(MainActivity.this, CitizenNavigation.class);
                startActivity(intent);
                finish();
            } else if (userType.equals("Emergency Service")) {
                // Redirect to Emergency Services Home Page
            /*
                Intent intent = new Intent(MainActivity.this, EmergencyServiceHomeActivity.class);
                startActivity(intent);
                finish();
            */

                Intent intent = new Intent(MainActivity.this, ServiceNavigation.class);
                startActivity(intent);
                finish();


            } else if (userType.equals("Expert")) {
                // Redirect to Expert Home Page
                Intent intent = new Intent(MainActivity.this, ExpertHomeActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void validate(String userName, String userPassword) {

        User_Name.setError(null);
        Password.setError(null);

        if (TextUtils.isEmpty(userName)) {
            User_Name.setError(getString(R.string.error_field_required));
            User_Name.requestFocus();
            Log.d("Login Activity", "Empty Username");
        } else if (TextUtils.isEmpty(userPassword)) {
            Password.setError(getString(R.string.error_field_required));
            Password.requestFocus();
        } else {

            Log.d("Sending Request ", userName + " " + userPassword);
            UserLoginTask uT = new UserLoginTask(userName, userPassword);
            uT.execute();
        }
    }

    public class UserLoginTask extends AsyncTask<String, String, String> {

        private final String username;
        private final String password;

        UserLoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(String... params) {

            String response;

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", password);

                HttpConnection httpConnection = new HttpConnection();
                response = httpConnection.doPostRequest("user/login", jsonObject);
                Log.d("LoginActivity", "Data from the Server: " + response);

                if (response == null)
                    return null;

                return response;


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String response) {


            if (response == null) {
                Log.d("LoginActivity", "Some error has occurred at Server");
                Toast.makeText(MainActivity.this, "Connect to Internet", Toast.LENGTH_LONG).show();
            } else if (response.equals("fail")) {
                Log.e("LoginActivity", "User in not Authentic");
                Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
            } else {

                Log.d("LoginActivity", "User in Authentic Display the Home page");

                if (response.equals("Citizen") || response.equals("Emergency Service") || response.equals("Expert")) {
                    UserDetailsTask uT = new UserDetailsTask(username, response);
                    uT.execute();

                    //Get the token of the user and store it in the backend.

                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.d("Error", "getInstanceId failed", task.getException());
                                        return;
                                    }

                                    idToken = task.getResult().getToken();
                                    session.setToken(idToken);
                                    String msg = getString(R.string.msg_token_fmt,idToken);
                                    TokenTask tok = new TokenTask(username,idToken);
                                    tok.execute();
                                    Log.d("Hello", msg);
                                }
                            });
                } else
                    Toast.makeText(MainActivity.this, "LoggedIn Failed", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            Log.d("LoginActivity", "Login Task Cancelled");
        }

    }


    public class UserDetailsTask extends AsyncTask<String, String, String> {

        private final String username;
        private final String user_category;

        UserDetailsTask(String username, String user_category) {
            this.username = username;
            if (user_category.equals("Citizen")) {
                user_category = "citizen";
            } else if (user_category.equals("Emergency Service")) {
                user_category = "serviceProvider";
            } else {
                user_category = "expert";
            }

            this.user_category = user_category;
        }

        @Override
        protected String doInBackground(String... params) {

            String response;

            try {


                HttpConnection httpConnection = new HttpConnection();
                response = httpConnection.doGetRequest(user_category + "/getDetails/" + username);

                Log.d("LoginActivity", "Data from the Server: " + response);


                JSONObject userObject = new JSONObject(response);

                // Store the user Details to the Session;

                if (userObject.getString("userCategory").equals("Citizen")) {
                    session.createCitizenLoginSession(userObject.getString("username"),
                            userObject.getString("password"), userObject.getString("email"),
                            userObject.getString("contactNumber"), userObject.getString("userCategory"), idToken
                    );
                } else if (userObject.getString("userCategory").equals("Emergency Service")) {

                    session.createServiceProviderLoginSession(userObject.getString("username"),
                            userObject.getString("password"), userObject.getString("email"),
                            userObject.getString("contactNumber"), userObject.getString("userCategory"),
                            (float) userObject.getDouble("latitude"), (float)userObject.getDouble("longitude"), userObject.getString("serviceCategory"),
                            idToken
                    );
                } else if (userObject.getString("userCategory").equals("Expert")) {

                    session.createExpertAdvisorLoginSession(userObject.getString("username"),
                            userObject.getString("password"), userObject.getString("email"),
                            userObject.getString("contactNumber"), userObject.getString("userCategory"), idToken,
                            userObject.getString("serviceCategory")
                    );

                }

                return userObject.getString("userCategory");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {

            if (response == null) {
                Log.d("LoginActivity", "Some error has occurred at Server");
                Toast.makeText(MainActivity.this, "Error getting user details", Toast.LENGTH_LONG).show();
            } else {

                Log.d("LoginActivity", "Token Stored Display the Home page");

                if (response.equals("Citizen")) {

                    Log.d("LoginActivity", "User : citizen");
                 /*
                    Toast.makeText(MainActivity.this, "Successfully LoggedIn", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, CitizenHomeActivity.class);
                    startActivity(intent);
                    finish();
                */
                    Intent intent = new Intent(MainActivity.this, CitizenNavigation.class);
                    startActivity(intent);
                    finish();

                } else if (response.equals("Emergency Service")) {

                    Toast.makeText(MainActivity.this, "Successfully LoggedIn", Toast.LENGTH_LONG).show();
                    Log.d("LoginActivity", "User : Service Provider");
                /*
                    Intent intent = new Intent(MainActivity.this, EmergencyServiceHomeActivity.class);
                    startActivity(intent);
                    finish();
                */
                    Intent intent = new Intent(MainActivity.this, ServiceNavigation.class);
                    startActivity(intent);
                    finish();

                } else if (response.equals("Expert")) {

                    Toast.makeText(MainActivity.this, "Successfully LoggedIn", Toast.LENGTH_LONG).show();
                    Log.d("Login Activity", "User : Expert Advisor");

                    Intent intent = new Intent(MainActivity.this, ExpertHomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }

        @Override
        protected void onCancelled() {
            Log.d("LoginActivity", "Login Task Cancelled");
        }
    }

}


class TokenTask extends AsyncTask<String, String, String> {

    private final String username;
    private final String token;

    TokenTask(String username, String token) {
        this.username = username;
        this.token = token;
    }

    @Override
    protected String doInBackground(String... params) {

        String response;

        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", username);
            jsonObject.put("token", token);

            HttpConnection httpConnection = new HttpConnection();
            response = httpConnection.doPostRequest("user/storeToken",jsonObject);

            Log.d("LoginActivity", "Data from the Server: " + response);

            return response;

        }

        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response){

        if (response == null) {
            Log.d("LoginActivity", "Some error has occurred at Server");
         //   Toast.makeText(MainActivity.this, "Some error occured. Try again", Toast.LENGTH_LONG).show();
        }

        else if (response.equals("fail")) {
            Log.e("LoginActivity", "Could not store the token");
         //   Toast.makeText(MainActivity.this, "Could not store the token", Toast.LENGTH_LONG).show();
        }

        else {

            Log.d("LoginActivity", "Token Stored Display the Home page");
        }
    }

    @Override
    protected void onCancelled() {
        Log.d("LoginActivity", "Login Task Cancelled");
    }
}


