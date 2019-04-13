package com.example.logindemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionUtil{

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "UserDetailsPref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // Username (make variable public to access from outside)
    public static  final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_CONTACT = "contact_number";
    public static final String KEY_SERVICE_CATEGORY = "service_category";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_USER_CATEGORY = "user_category";
    public static final String KEY_TOKEN = "token";

    // Constructor
    public SessionUtil(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    //Save Citizen Details After Login

    public void createCitizenLoginSession(String username,String password,String email,String contact,String user_category,String token){

        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in Shared preferences

        setUsername(username);
        setPassword(password);
        setEmail(email);
        setContact(contact);
        setUserCategory(user_category);
        if(token!=null)
            setToken(token);

        // commit changes to the file.
        editor.commit();
    }

    public void createServiceProviderLoginSession(String username,String password,String email,String contact,String user_category,float latitude,float longitude,String service_category,String token){

        editor.putBoolean(IS_LOGIN,true);

        setUsername(username);
        setPassword(password);
        setEmail(email);
        setContact(contact);
        setUserCategory(user_category);
        if(token!=null)
        setToken(token);
        setLatitue(latitude);
        setLongitude(longitude);
        setServiceCategory(service_category);

        editor.commit();
    }

    public void createExpertAdvisorLoginSession(String username,String password,String email,String contact,String user_category,String token,String service_category){


        editor.putBoolean(IS_LOGIN,true);

        setUsername(username);
        setPassword(password);
        setEmail(email);
        setContact(contact);
        setUserCategory(user_category);
        if(token!=null)
        setToken(token);
        setServiceCategory(service_category);

    }

    public String getEmail(){
        return pref.getString(KEY_EMAIL,null);
    }

    public void setEmail(String email){
        editor.putString(KEY_EMAIL,email);
    }

    public String getUsername(){
        return pref.getString(KEY_USERNAME,null);
    }

    public void setUsername(String username){
       editor.putString(KEY_USERNAME,username);
    }

    public String getPassword(){
        return pref.getString(KEY_PASSWORD,null);
    }

    public void setPassword(String password){
        editor.putString(KEY_PASSWORD,password);
    }

    public String getContact(){
        return pref.getString(KEY_CONTACT,null);
    }

    public void setContact(String contact){
        editor.putString(KEY_CONTACT,contact);
    }

    public String getUserCategory(){
        return pref.getString(KEY_USER_CATEGORY,null);
    }

    public void setUserCategory(String userCategory){
        editor.putString(KEY_USER_CATEGORY,userCategory);
    }

    public String getServiceCategory(){
        return pref.getString(KEY_SERVICE_CATEGORY,null);
    }

    public void setServiceCategory(String serviceCategory){
        editor.putString(KEY_SERVICE_CATEGORY,serviceCategory);
    }

    public float getLatitude(){
        return pref.getFloat(KEY_LATITUDE,0);
    }

    public void setLatitue(float latitue){
        editor.putFloat(KEY_LATITUDE,latitue);
    }

    public double getLongitude(){
        return pref.getFloat(KEY_LONGITUDE,0);
    }

    public void setLongitude(float longitude){
        editor.putFloat(KEY_LONGITUDE,longitude);
    }

    public String getToken(){
        return pref.getString(KEY_TOKEN,null);
    }

    public void setToken(String token){
        editor.putString(KEY_TOKEN,token);
    }


    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN,false);
    }

    public void logoutUser(){

        editor.clear();
        editor.commit();
        Intent intent = new Intent(context, MainActivity.class);
        // Closing all the Activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Start Login Activity
        context.startActivity(intent);

    }

    /*
    public void editUserDetails(String name, String age, String gender, String email, String mobile){
        editor.putString(KEY_NAME,name);
        editor.putString(KEY_EMAIL,email);
        editor.putString(KEY_CONTACT,mobile);
        editor.commit();
    }



    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME,pref.getString(KEY_NAME,null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // user email id
        user.put(KEY_CONTACT, pref.getString(KEY_CONTACT, null));


        // return user
        return user;
    }*/

}