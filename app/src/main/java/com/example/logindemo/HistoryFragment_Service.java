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

public class HistoryFragment_Service extends Fragment
{
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;



    public static HistoryFragment_Service newInstance() {
        return new HistoryFragment_Service();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, null);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragmentHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        FetchData fd = new FetchData(getContext());
        fd.execute();

        return rootView;
    }

    @Override
    public String toString() {
        return "History";
    }

    public class FetchData extends AsyncTask<String, String, String> {

        private final Context context;

        FetchData(Context context) {
            this.context = context;

        }

        String requestId = "";
        Double latitude;
        Double longitude;
        String Status = "";

        @Override
        protected String doInBackground(String... params) {

            String response;
            HttpConnection httpConnection = new HttpConnection();
            SessionUtil session = new SessionUtil(context);
            response = httpConnection.doGetRequest("serviceProvider/pastRequests/" + session.getUsername());
            if(response.equals("[]")){
                return null;
            }

            return response;
        }


        @Override
        protected void onPostExecute(String response){
            if(response==null){
                Log.d("Citizen All Requests","No Requests or Error At Server");
                Toast.makeText(context,"No Requests",Toast.LENGTH_SHORT).show();
            }
            else
            {
               List<RequestWithId> listitems;
                Log.d("Past Request","Display the requests " + response);
                try{
                    listitems = new ArrayList<RequestWithId>();
                    JSONArray requests_array = new JSONArray(response);

                    for (int i = 0; i < requests_array.length(); i++) {
                        JSONObject JO = (JSONObject) requests_array.get(i);
                        requestId = JO.getString("requestId");
                        latitude = JO.getDouble("latitude");
                        longitude = JO.getDouble("longitude");
                        Status = JO.getString("status");
                        String citizen = JO.getString("citizen");
                        String expert = JO.getString("expert");
                        listitems.add(new RequestWithId(requestId,citizen,latitude,longitude,Status,expert));
                     //   recyclerView.setAdapter(new HistoryAdapter_Service(listitems, getActivity()));
                    }
                    if(listitems.size()>0) {
                        recyclerView.setAdapter(new HistoryAdapter_Service(listitems, getActivity()));
                    }
                    else {
                        Toast.makeText(getContext(),"No Past History",Toast.LENGTH_SHORT).show();
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
