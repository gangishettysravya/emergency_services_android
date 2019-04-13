package com.androidchatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;


public class Chat extends AppCompatActivity {
    LinearLayout layout1;
    RelativeLayout layout2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase fobj1, fobj2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout1 = (LinearLayout) findViewById(R.id.layout1);
        layout2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        Firebase.setAndroidContext(this);
        fobj1 = new Firebase("https://chatapp-d1afb.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.receiver);
        fobj2 = new Firebase("https://chatapp-d1afb.firebaseio.com/messages/" + UserDetails.receiver + "_" + UserDetails.username);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", UserDetails.username);
                    fobj1.push().setValue(map);
                    fobj2.push().setValue(map);
                    messageArea.setText("");
                }
            }
        });

        fobj1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map hmap = dataSnapshot.getValue(Map.class);
                String message = hmap.get("message").toString();
                String userName = hmap.get("user").toString();

                if(userName.equals(UserDetails.username)){
                    addMessageBox("You:\n" + message, 1);
                }
                else{
                    addMessageBox(UserDetails.receiver + ":\n" + message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);

        LinearLayout.LayoutParams linp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linp2.weight = 1.0f;

        if(type == 1) {
            linp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        }
        else{
            linp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(linp2);
        layout1.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}