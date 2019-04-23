package com.example.logindemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.firebase.client.Firebase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Chat extends AppCompatActivity{
    private static final String TAG = "Chat";

    LinearLayout layout1;
    RelativeLayout layout2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    String username;
    String expert;
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_USER = "user";
    String timeStamp;

    private FirebaseFirestore db;
    private CollectionReference messageRef;
    static int i=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        SessionUtil session=new SessionUtil(getApplicationContext());
        username=session.getUsername();
        UserDetails.sender=username;
        db=FirebaseFirestore.getInstance();
        messageRef = db.collection("Chat").document(UserDetails.sender+"_"+UserDetails.receiver).collection("Message");

        layout1 = (LinearLayout) findViewById(R.id.layout1);
        layout2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        Firebase.setAndroidContext(this);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();


                if(!messageText.equals("")){
                    Map<String, String> hmap = new HashMap<String, String>();
                    hmap.put("message", messageText);
                    hmap.put("user", username);
                    timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                    db.collection("Chat").document(UserDetails.sender+"_"+UserDetails.receiver).collection("Message").document("msg"+timeStamp).set(hmap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Chat.this, "Message sent", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Chat.this, "message sending failed", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, e.toString());
                                }
                            });
                    db.collection("Chat").document(UserDetails.receiver+"_"+UserDetails.sender).collection("Message").document("msg"+timeStamp).set(hmap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Chat.this, "Message received", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Chat.this, "message not received", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, e.toString());
                                }
                            });
                    i++;
                    messageArea.setText("");
                }
            }
        });

        messageRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    Map hmap1=dc.getDocument().getData();
                    Object message = hmap1.get(KEY_MESSAGE);
                    Object userName = hmap1.get(KEY_USER);

                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "New message: " + dc.getDocument().getData());

                            if (userName.equals(UserDetails.sender))
                            {
                                addMessageBox("You:\n" + message, 1);
                            }
                            else
                            {
                                addMessageBox(UserDetails.receiver + ":\n" + message, 2);
                            }
                            break;
                        case MODIFIED:
                            Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                            break;
                    }
                }

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
