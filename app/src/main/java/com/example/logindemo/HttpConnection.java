package com.example.logindemo;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpConnection {

    private String server_url = "http://172.16.144.217:8080/services/webapi/";

    public HttpConnection(){

    }

    public String doGetRequest(String api_url){

        try {

            String serverURL = server_url + api_url;
            Log.d("In Server Request",serverURL);
            URL url = new URL(serverURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            Log.d("Code", "ResponseCode: " + responseCode);

            InputStream is = connection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder total = new StringBuilder();

            String response;

            while ((response = r.readLine()) != null) {
                total.append(response);
            }

            is.close();

            if (total.length() == 0) {
                return null;
            }

            response = total.toString();
            response = response.trim();

            return response;
        }

        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public String doPostRequest(String api_url, JSONObject jsonObject){

        try {

            String serverURL = server_url + api_url;
            URL url = new URL(serverURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json ; charset=UTF-8");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            os.write(jsonObject.toString().getBytes("UTF-8"));
            os.close();

            int responseCode = connection.getResponseCode();

            Log.d("Code", "ResponseCode: " + responseCode);

            InputStream is = connection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder total = new StringBuilder();

            String response;

            while ((response = r.readLine()) != null) {
                total.append(response);
            }

            is.close();

            if (total.length() == 0) {
                return null;
            }

            response = total.toString();
            response = response.trim();

            return response;
        }

        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
