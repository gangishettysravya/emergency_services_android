package com.example.logindemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class CitizenRequest extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    String selectedCategory=null;
    String userChosenOption = null;
    double latitude;
    double longitude;
    Bitmap bitmap = null;
    double distance =10;
    public static final int MY_PERMISSIONS_REQUEST_STORAGE_ACCESS = 98;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 97;

    ImageView sceneImage;
    TextView DescriptionView;
    //Map<String, List<String>> servicesAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citizen_request);

        Intent intent = this.getIntent();

        if(intent!=null){
            this.latitude = intent.getExtras().getDouble("latitude");
            this.longitude = intent.getExtras().getDouble("longitude");
            this.distance = intent.getExtras().getDouble("distance");
    //        MyMap services = (MyMap) intent.getExtras().getSerializable("servicesAvailable");
    //        this.servicesAvailable = services.servicesAvailable;
    //        Log.d("The Services are",servicesAvailable.toString());
        }

        Spinner spinner = (Spinner)findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.category_array,
                android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        sceneImage = (ImageView) findViewById(R.id.sceneImageView);
        DescriptionView = (TextView) findViewById(R.id.description_tv);

    }

    public void onClickUploadImage(View v) {
        chooseImage();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
        this.selectedCategory = parent.getItemAtPosition(pos).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE_ACCESS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        galleryIntent();
                } else {
                    Toast.makeText(CitizenRequest.this,"Need Storage Access To Upload Photo",Toast.LENGTH_LONG).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraIntent();
                }else{
                    Toast.makeText(CitizenRequest.this,"Need Camera Access",Toast.LENGTH_LONG).show();
                }
        }
    }


    public void chooseImage(){

        final CharSequence[] items = {"Take a photo","Choose from Library","Cancel"};

        AlertDialog.Builder builder =  new AlertDialog.Builder(CitizenRequest.this);
        builder.setTitle("Upload Image");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if(items[item].equals("Take a photo")) {
                    boolean result= checkPermission(CitizenRequest.this,"camera");
                    userChosenOption = items[item].toString();
                    if(result)
                        cameraIntent();
                }

                else if(items[item].equals("Choose from Library")){
                    boolean result= checkPermission(CitizenRequest.this,"library");
                    userChosenOption = items[item].toString();

                    if(result)
                        galleryIntent();
                }

                else if(items[item].equals("Cancel")){
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    public void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    public void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    public void sendRequest(View view){
        if(validate()){
            SessionUtil session = new SessionUtil(getApplicationContext());
            CitizenRequestTask citizentask = new CitizenRequestTask(session.getUsername(), latitude, longitude, null, selectedCategory,DescriptionView.getText().toString(), distance);
            citizentask.execute();
        }
    }

    public boolean validate(){

        DescriptionView.setError(null);

        if(selectedCategory==null){
            Toast.makeText(CitizenRequest.this,"Choose a Category",Toast.LENGTH_LONG).show();
            return false;
        }


        if(TextUtils.isEmpty(DescriptionView.getText())){
            DescriptionView.setError(getString(R.string.error_field_required));
            DescriptionView.requestFocus();
            return false;
        }

        if(bitmap==null){
            Toast.makeText(CitizenRequest.this,"Upload an Image",Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap = thumbnail;

        //Compress and resize the image here
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sceneImage.setImageBitmap(thumbnail);
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                bitmap = bm;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sceneImage.setImageBitmap(bm);
    }

    public static boolean checkPermission(final Context context,String selection)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if(selection.equals("library")) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                        alertBuilder.setCancelable(true);
                        alertBuilder.setTitle("Permission necessary");
                        alertBuilder.setMessage("External storage permission is necessary");
                        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE_ACCESS);
                            }
                        });
                        AlertDialog alert = alertBuilder.create();
                        alert.show();
                        return false;

                    } else {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE_ACCESS);
                        return false;
                    }
                } else {
                    return true;
                }
            }
            else{
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.CAMERA)) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                        alertBuilder.setCancelable(true);
                        alertBuilder.setTitle("Permission necessary");
                        alertBuilder.setMessage("External storage permission is necessary");
                        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                            }
                        });
                        AlertDialog alert = alertBuilder.create();
                        alert.show();
                        return false;
                    } else {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA},MY_PERMISSIONS_REQUEST_CAMERA);
                        return false;
                    }
                } else {
                    return true;
                }
            }
        } else {
            return true;
        }
    }


    public class CitizenRequestTask extends AsyncTask<String, String, String> {

        private final String username;
        private final double latitude;
        private final double longitude;
        private final Image image;
        private final String serviceCategory;
        private final String description;
        private double distance;


        CitizenRequestTask(String username, double latitude,double longitude,Image image,String service_category,String description,double distance) {
            this.username = username;
            this.latitude = latitude;
            this.longitude = longitude;
            this.image = image;
            this.serviceCategory = service_category;
            this.distance = distance;
            this.description = description;
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                String serverURL = "http://172.16.144.217:8080/services/webapi/citizen/sendRequest";
                URL url = new URL(serverURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                String boundary = UUID.randomUUID().toString();
                connection.setRequestProperty("Content-Type",  "multipart/form-data;boundary=" + boundary);
                connection.setDoOutput(true);

                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                //OutputStream os = connection.getOutputStream();
                //os.write(jsonObject.toString().getBytes("UTF-8"));
                //os.close();

                os.writeBytes("--" + boundary + "\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"username\"\r\n\r\n");
                os.writeBytes(username + "\r\n");

                os.writeBytes("--" + boundary + "\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"serviceCategory\"\r\n\r\n");
                os.writeBytes(serviceCategory + "\r\n");

                os.writeBytes("--" + boundary + "\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"latitude\"\r\n\r\n");
                os.writeBytes(latitude + "\r\n");

                os.writeBytes("--" + boundary + "\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"longitude\"\r\n\r\n");
                os.writeBytes(longitude+ "\r\n");

                os.writeBytes("--" + boundary + "\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"distance\"\r\n\r\n");
                os.writeBytes(distance+"\r\n");

                os.writeBytes("--" + boundary + "\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n");
                os.writeBytes(description+"\r\n");


                os.writeBytes("--" + boundary + "\r\n");
                os.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" + "sceneImage" + "\"\r\n\r\n");

                /*
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                */

                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                os.writeBytes("\r\n");
                os.writeBytes("--" + boundary + "--\r\n");
                os.flush();

                int responseCode = connection.getResponseCode();

                Log.d("Code", "ResponseCode: " + responseCode);

                InputStream is = connection.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                is.close();
                line = total.toString();
                line = line.trim();
                Log.d("LoginActivity", "Data from the Server: " + line);

                return line;

            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
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
                Log.d("LoginActivity", "Some error has occurred at Server");
                Toast.makeText(CitizenRequest.this,"Some error occured. Try again",Toast.LENGTH_LONG).show();
            }
            else if(s.equals("fail"))
            {
                Log.e("CitizenRequestActivity", "Could not send request");
                if(distance==10) {
                    CitizenRequestTask cr = new CitizenRequestTask(username, latitude, longitude, null, selectedCategory,description, 15);
                    cr.execute();
                    Toast.makeText(CitizenRequest.this, "Services Not Available in Selected Category within 10 Km. Searching for Services within 15km", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(CitizenRequest.this,"Services Not Available in Selected Category within 15km",Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                Log.d("CitizenRequestActivity","Request Sent to backend");
                Toast.makeText(CitizenRequest.this,"Request Sent Successfully",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(CitizenRequest.this, CitizenNavigation.class);
                startActivity(i);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            Log.d("LoginActivity","Login Task Cancelled");
        }
    }

}