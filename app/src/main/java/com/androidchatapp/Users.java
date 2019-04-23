
package com.example.logindemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.firebase.client.Firebase;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.*;

public class Users extends AppCompatActivity {

    private static final String TAG = "Users";
    ListView usersList;
    TextView noUsersText;
    ArrayList<String> arrlist;
    static int totalUsers=0;
    ProgressDialog pd;
    private FirebaseFirestore ff;
    private CollectionReference requestRef;
    String expert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        ff = FirebaseFirestore.getInstance();
        requestRef = ff.collection("requests");
        Log.d(TAG, "Created list");

        SessionUtil session = new SessionUtil(getApplicationContext());
        expert = session.getUsername();
        usersList = (ListView) findViewById(R.id.usersList);
        noUsersText = (TextView) findViewById(R.id.noUsersText);
        arrlist = new ArrayList<>();

        pd = new ProgressDialog(Users.this);
        pd.setMessage("Loading...");
        pd.show();
        Log.d(TAG, expert );

        Task task1 = requestRef.whereEqualTo("expert", expert).whereEqualTo("status","Ongoing").get();
        Task task2 = requestRef.whereEqualTo("expert", expert).whereEqualTo("status","New").get();

        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(task1,task2);
        allTasks.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {

               for(QuerySnapshot value : querySnapshots) {
                   for (QueryDocumentSnapshot doc : value) {
                       Log.d(TAG, "inside for");

                       if (doc.get("citizen") != null) {
                           totalUsers++;
                           Log.d(TAG, "Query");
                           arrlist.add(doc.getString("citizen"));

                       }
                   }
                   doOnSuccess(arrlist);
                   Log.d(TAG, "Query ended");
                   Log.d(TAG, arrlist.get(0));
               }
            }
        });


        /*
        requestRef.whereEqualTo("expert", expert).whereEqualTo("status").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    Log.d(TAG, "inside for");

                    if (doc.get("citizen") != null) {
                        totalUsers++;
                        Log.d(TAG, "Query");
                        arrlist.add(doc.getString("citizen"));

                    }
                }
                doOnSuccess(arrlist);
                Log.d(TAG, "Query ended");
                Log.d(TAG, arrlist.get(0));


            }

        });*/

//        doOnSuccess(arrlist);

   /*     String url = "https://chatapp-d1afb.firebaseio.com/users.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                doOnSuccess(s);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(ShowUsers.this);
        rQueue.add(request);*/

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserDetails.receiver = arrlist.get(position);
                startActivity(new Intent(Users.this, Chat.class));
            }
        });
    }
    /*protected void onStart() {
        super.onStart();
        requestRef.whereEqualTo("expert", expert).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("citizen") != null) {
                        arrlist.add(doc.getString("citizen"));
                        totalUsers++;
                    }
                }
                Log.d(TAG, "Current citizens" );
                doOnSuccess();
            }
        });
                for (QueryDocumentSnapshot doc : value) {
                    Log.d(TAG, "inside for");

                    if (doc.get("citizen") != null) {
                        totalUsers++;
                        Log.d(TAG, "Query");
                        arrlist.add(doc.getString("citizen"));

                    }
                }
                Log.d(TAG, "Query ended");
                Log.d(TAG, arrlist.get(0));

    }*/

    public void doOnSuccess(ArrayList<String> arrlist) {


        Log.d(TAG, "On success");
        Log.d(TAG, arrlist.get(0));

        if (totalUsers < 1) {
            noUsersText.setVisibility(View.VISIBLE);
            usersList.setVisibility(View.GONE);
        } else {
            noUsersText.setVisibility(View.GONE);
            usersList.setVisibility(View.VISIBLE);
            usersList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrlist));
             }

            pd.dismiss();
        }
    }