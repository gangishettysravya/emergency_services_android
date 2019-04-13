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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

// Created By Manisha
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {
    private EditText Name;
    private EditText Create_Password;
    private Button next;
    String st=null;
    String idToken;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setupUIViews();

        //spinner code
        spinner=(Spinner)findViewById(R.id.spinner);
        List<String> list=new ArrayList<String>();
        list.add("Choose Category");
        list.add("Emergency Service");
        list.add("Citizen");
        list.add("Expert");

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner.setSelection(position);
                st=parent.getItemAtPosition(position).toString();
Log.d("value", st);
            }
             @Override
             public void onNothingSelected(AdapterView<?> parent) {
                 Toast.makeText(RegistrationActivity.this, "Please select a category", Toast.LENGTH_SHORT).show();
             }

        });


        /*FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken = task.getResult().getToken();
                            // Send token to your backend via HTTPS


                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });*/


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()) {

                    checkUserTask cT = new checkUserTask(Name.getText().toString());
                    cT.execute();
                }
            }
        });

        //firebaseAuth=FirebaseAuth.getInstance();

        /*Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(validate())
               {
                   //upload data to database
                   String user_Email=Email.getText().toString().trim(); //trim is to remove whitespaces
                   String user_Password=Create_Password.getText().toString().trim();

                   firebaseAuth.createUserWithEmailAndPassword(user_Email, user_Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           if(task.isSuccessful()) {
                               Toast.makeText(RegistrationActivity.this, "Registration is successful", Toast.LENGTH_SHORT).show();
                               startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                           }
                           else{
                               Toast.makeText(RegistrationActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();

                           }

                       }
                   });
               }
            }
        });*/
    }

   private void setupUIViews(){
       Name = (EditText)findViewById(R.id.etusername);
       Create_Password = (EditText)findViewById(R.id.etPaswd);
       next = (Button)findViewById(R.id.btnNext);
       //Register = (Button)findViewById(R.id.btnRegister);


   }

   private boolean validate()
   {
       boolean result=false;

       Name.setError(null);
       Create_Password.setError(null);
       String name = Name.getText().toString();
       String password=Create_Password.getText().toString();

       if (TextUtils.isEmpty(name)) {
           Name.setError(getString(R.string.error_field_required));
           Name.requestFocus();
           Log.d("Registration Activity","Empty Username");
       } else if(TextUtils.isEmpty(password)) {
           Create_Password.setError(getString(R.string.error_field_required));
       }
       else if(st.equals("Choose Category")){
           Toast.makeText(RegistrationActivity.this, "Please select a category", Toast.LENGTH_SHORT).show();
       }
       else {

           result = true;
       }

       return  result;

   }

    public class checkUserTask extends AsyncTask<String, String, String> {

        private final String username;

        checkUserTask(String username) {
            this.username = username;
        }

        @Override
        protected String doInBackground(String... params) {

            String response;

            HttpConnection httpConnection = new HttpConnection();
            response = httpConnection.doGetRequest("user/checkUser/"+username);

            Log.d("Registration Activity", "Data from the Server: " + response);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {

            if (response == null) {
                Log.d("LoginActivity", "Some error has occurred at Server");
                Toast.makeText(RegistrationActivity.this, "Error getting user details", Toast.LENGTH_LONG).show();
            } else {

                if(response.equals("valid")){
                    Intent i = new Intent(RegistrationActivity.this, ServiceActivity.class);
                    i.putExtra("Value", st);
                    i.putExtra("username", Name.getText().toString());
                    i.putExtra("password", Create_Password.getText().toString());
                    startActivity(i);
                }

                else {

                    Name.setError(getString(R.string.error_username_alreadyexists));
                    Name.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            Log.d("LoginActivity", "Login Task Cancelled");
        }
    }

}
