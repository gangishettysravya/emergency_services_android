package com.example.logindemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

// Created by Manisha

public class ServiceActivity extends AppCompatActivity {

    //write logic of hide

    private TextView alreadySigned;
    private Button Register;
    private Button Next; //for service providers to enter location
    private EditText Email;
    private EditText Mobile;

     String st;
     String username;
     String password;
     String category;


    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service2);
        setupUIViews();

        st= getIntent().getExtras().getString("Value");
        username=getIntent().getExtras().getString("username");
        password=getIntent().getExtras().getString("password");


        alreadySigned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ServiceActivity.this, MainActivity.class));
                finish();
                //if already account, redirect to login page
            }
        });

        if(st.equals("Citizen"))
        {
            spinner.setVisibility(View.GONE);
            Next.setVisibility(View.GONE);
        }

        if(st.equals("Expert"))
        {
            Next.setVisibility(View.GONE);
        }

        if(st.equals("Emergency Service"))
        {
            Register.setVisibility(View.GONE);
        }

        spinner=(Spinner)findViewById(R.id.spinner2);
        List<String> list=new ArrayList<String>();
        list.add("Select Service");
        list.add("Hospital");
        list.add("Fire Station");
        list.add("Police Station");

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner.setSelection(position);
                category=parent.getItemAtPosition(position).toString();
                Log.d("service_category", category);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                category = "Select Service";
            }
        });

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate(Email.getText().toString(), Mobile.getText().toString())){
                        checkEmailTask eT = new checkEmailTask(Email.getText().toString());
                        eT.execute();
                }
            }
        });

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate(Email.getText().toString(),Mobile.getText().toString())) {
                    checkEmailTask eT = new checkEmailTask(Email.getText().toString());
                    eT.execute();
                }
            }
        });
    }

    private void setupUIViews(){
        alreadySigned = (TextView)findViewById(R.id.tvalreadysigned);
        Register = (Button)findViewById(R.id.btnRegister);
        Next = (Button)findViewById(R.id.btnNext);
        Email=(EditText)findViewById(R.id.etUserEmail);
        Mobile=(EditText)findViewById(R.id.etMobile);
        spinner = (Spinner)findViewById(R.id.spinner2);
    }


    private boolean isValidEmail(String email) {
        return email.contains("@");
    }

    private boolean isValidPhone(String mobile){
        return mobile.length() == 10;
    }

    private boolean validate(String email, String contact_number)
    {

        boolean result=false;

        Email.setError(null);
        Mobile.setError(null);

        if (TextUtils.isEmpty(email)) {
            Email.setError(getString(R.string.error_field_required));
            Email.requestFocus();
            Log.d("Service Activity","Empty Email");
        } else if(TextUtils.isEmpty(contact_number)) {
            Mobile.setError(getString(R.string.error_field_required));
        }
        else if(!isValidEmail(email)){
            Email.setError(getString(R.string.error_invalid_email));
        }
        else if(!isValidPhone(contact_number)) {
            Mobile.setError(getString(R.string.error_invalid_mobile));
        }
        else if((category==null || category.equals("Select Service") )&& (!st.equals("Citizen"))){
            Toast.makeText(ServiceActivity.this, "Please select a service category", Toast.LENGTH_SHORT).show();
        }
        else {

            result = true;
        }

        return  result;

    }


    public class checkEmailTask extends AsyncTask<String, String, String> {

        private final String email;

        checkEmailTask(String email) {
            this.email = email;
        }

        @Override
        protected String doInBackground(String... params) {

            String response;

            HttpConnection httpConnection = new HttpConnection();
            response = httpConnection.doGetRequest("user/checkEmail/"+email);

            Log.d("Registration Activity", "Data from the Server: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            if (response == null) {
                Log.d("LoginActivity", "Some error has occurred at Server");
                Toast.makeText(ServiceActivity.this, "Error getting email validity", Toast.LENGTH_LONG).show();
            } else {

                if(response.equals("valid")){
                    if(st.equals("Citizen")){
                        //SignUp as a citizen
                        CreateCitizenTask cC = new CreateCitizenTask(username, password,st,Email.getText().toString(),Mobile.getText().toString());
                        cC.execute();

                    }

                    else if(st.equals("Expert")){

                        CreateExpertTask cE = new CreateExpertTask(username, password, st, category, Email.getText().toString(), Mobile.getText().toString());
                        cE.execute();

                    }

                    else if(st.equals("Emergency Service")){
                        Intent i = new Intent(ServiceActivity.this, ServiceProviderLocationChooserActivity.class);
                        i.putExtra("userCategory", st);
                        i.putExtra("username",username);
                        i.putExtra("password",password);
                        i.putExtra("email",Email.getText().toString());
                        i.putExtra("contactNumber",Mobile.getText().toString());
                        i.putExtra("serviceCategory",category);
                        startActivity(i);
                    }


                }

                else {

                    Email.setError(getString(R.string.error_email_alreadyexists));
                    Email.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            Log.d("LoginActivity", "Login Task Cancelled");
        }
    }


    public class CreateCitizenTask extends AsyncTask<String, String, String> {

        private String username;
        private String email;
        private String contactNumber;
        private String password;
        private String userCategory;

        CreateCitizenTask(String username, String password,String user_category,String email, String contact_number){
                this.username = username;
                this.password = password;
                this.email = email;
                this.contactNumber = contact_number;
                this.userCategory = user_category;
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO: attempt authentication against a network service.

            String result;

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", password);
                jsonObject.put("email", email);
                jsonObject.put("contactNumber", contactNumber);
                jsonObject.put("userCategory",userCategory);

                HttpConnection httpConnection = new HttpConnection();

                result = httpConnection.doPostRequest("citizen/signup",jsonObject);

                return result;

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
                if(s == null)
                {
                    Log.d("ServiceActivity", "Some error has occurred at Server");
                    Toast.makeText(ServiceActivity.this,"Some error occured. Try again",Toast.LENGTH_LONG).show();
                }
                else if(s.equals("fail")) {
                   Log.e("ServiceActivity", "Couldnot add to the data");
                   Toast.makeText(ServiceActivity.this,"Something went wrong. Try again",Toast.LENGTH_LONG).show();
                }
                else {

                    Log.d("ServiceActivity", "User is Registered Display the Home page");

                    Toast.makeText(ServiceActivity.this,"Successfully Registered",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ServiceActivity.this, MainActivity.class);
                    startActivity(intent);

                    finish();
                }
        }

        @Override
        protected void onCancelled() {
            Log.d("RegistrationActivity","Registration Task Cancelled");
        }
    }


    public class CreateExpertTask extends AsyncTask<String, String, String> {

        private String username;
        private String email;
        private String contactNumber;
        private String password;
        private String userCategory;
        private String serviceCategory;

        CreateExpertTask(String username, String password,String user_category,String service_category,String email, String contact_number){
            this.username = username;
            this.password = password;
            this.email = email;
            this.contactNumber = contact_number;
            this.userCategory = user_category;
            this.serviceCategory = service_category;
        }

        @Override
        protected String doInBackground(String... params) {

            String result;

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", password);
                jsonObject.put("email", email);
                jsonObject.put("contactNumber", contactNumber);
                jsonObject.put("userCategory",userCategory);
                jsonObject.put("serviceCategory",serviceCategory);

                HttpConnection httpConnection = new HttpConnection();

                result = httpConnection.doPostRequest("expert/signup",jsonObject);

                return result;

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s == null)
            {
                Log.d("ServiceActivity", "Some error has occurred at Server");
                Toast.makeText(ServiceActivity.this,"Some error occured. Try again",Toast.LENGTH_LONG).show();
            }
            else if(s.equals("fail")) {
                Log.e("ServiceActivity", "Could not add the data");
                Toast.makeText(ServiceActivity.this,"Something went wrong. Try again",Toast.LENGTH_LONG).show();
            }

            else {

                Log.d("ServiceActivity", "User is Registered Display the Login page");

                Toast.makeText(ServiceActivity.this,"Successfully Registered",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ServiceActivity.this, MainActivity.class);
                startActivity(intent);

                finish();
            }
        }
        @Override
        protected void onCancelled() {
            Log.d("RegistrationActivity","Registration Task Cancelled");
        }
    }


}
