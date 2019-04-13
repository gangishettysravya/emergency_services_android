package com.example.logindemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CitizenAllRequests extends Fragment {

    private List<RequestWithId> requests;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.display_citizen_requests, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("All Requests");

        SessionUtil session = new SessionUtil(getContext());
        getAllRequestsTask requestsTask = new getAllRequestsTask(session.getUsername());
        requestsTask.execute();
    }

    public class getAllRequestsTask extends AsyncTask<String, String, String> {

        String username;

        getAllRequestsTask(String username){
            this.username = username;
        }

        @Override
        protected String doInBackground(String...params){

            HttpConnection httpConnection = new HttpConnection();
            String response = httpConnection.doGetRequest("citizen/getAllRequests/"+username);

            return response;
        }

        @Override
        protected void onPostExecute(String response){

            if(response==null){
                Log.d("Citizen All Requests","No Requests or Error At Server");
                Toast.makeText(getContext(),"No Requests",Toast.LENGTH_SHORT).show();
            }

            else{

                Log.d("Citizen All Requests","Display the requests " + response);
                try{

                    String requestId,citizen,serviceCategory,acceptedBy,description,status;
                    double latitude,longitude;
                    requests = new ArrayList<RequestWithId>();
                    JSONArray requests_array = new JSONArray(response);

                    for(int i=0;i<requests_array.length();i++){

                        JSONObject req = requests_array.getJSONObject(i);

                        requestId = req.getString("requestId");
                        citizen = req.getString("citizen");
                        serviceCategory = req.getString("serviceCategory");
                        acceptedBy = req.getString("acceptedBy");
                        description = req.getString("description");
                        status = req.getString("status");
                        latitude = req.getDouble("latitude");
                        longitude = req.getDouble("longitude");

                        //TODO SET AN EXPERT
                        requests.add(new RequestWithId(citizen,serviceCategory,description,latitude,longitude,status,acceptedBy,requestId,""));

                    }

                    if(requests.size()==0){
                        Toast.makeText(getContext(),"No Requests Available",Toast.LENGTH_SHORT).show();
                    }

                    else {
                        ArrayAdapter<RequestWithId> adapter = new CitizenAllRequestsAdapter(getActivity(), 0, requests);
                        ListView listView = (ListView) getActivity().findViewById(R.id.customListView);
                        listView.setAdapter(adapter);
                    }
                }

                catch(Exception e){

                    Log.d("Citizen All Requests : ",e.getMessage());
                    e.printStackTrace();

                }


            }

        }

        @Override
        protected void onCancelled() {

        }
    }

}
