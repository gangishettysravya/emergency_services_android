package com.example.logindemo;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RequestFragment_Service extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    public static RequestFragment_Service newInstance() {
        return new RequestFragment_Service();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_request, null);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragmentrequest);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        FetchData fd = new FetchData(getContext());
        fd.execute();

        //  recyclerView.setAdapter(new NewRequestAdapter(listItems, getActivity()));
        return rootView;
    }

    @Override
    public String toString() {
        return "Request";
    }
    public class FetchData extends AsyncTask<String, String, String> {

        private final Context context;

        FetchData(Context context) {
            this.context = context;

        }

        String requestId = "";
        Double latitude;
        Double longitude;

        @Override
        protected String doInBackground(String... params) {

            String response;
            HttpConnection httpConnection = new HttpConnection();
            SessionUtil session = new SessionUtil(context);
            response = httpConnection.doGetRequest("serviceProvider/newRequests/" + session.getUsername());


            return response;
        }


        @Override
        protected void onPostExecute(String response){
            if(response==null){
                Log.d("Citizen All Requests","No Requests or Error At Server");
                Toast.makeText(context,"No Requests",Toast.LENGTH_SHORT).show();
            }
            else
            {    List<RequestWithId> listItems;

                Log.d("Past Request","Display the requests " + response);
                try{
                    listItems = new ArrayList<RequestWithId>();
                    JSONArray requests_array = new JSONArray(response);

                    for (int i = 0; i < requests_array.length(); i++) {
                        JSONObject JO = (JSONObject) requests_array.get(i);
                        requestId = JO.getString("requestId");
                        latitude = JO.getDouble("latitude");
                        longitude = JO.getDouble("longitude");
                        String citizen = JO.getString("citizen");
                        listItems.add(new RequestWithId(requestId,citizen,latitude,longitude));
                    }
                    if(listItems.size()>0){
                        recyclerView.setAdapter(new NewRequestAdapter(listItems, getActivity()));
                    }
                    else{
                        Toast.makeText(getContext(),"No Requests",Toast.LENGTH_SHORT).show();
                    }

                }
                catch(Exception e){

                    Log.d("Citizen All Requests : ", e.getMessage());
                    e.printStackTrace();

                }
            }
        }
    }

}