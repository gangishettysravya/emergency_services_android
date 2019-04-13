package com.example.logindemo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NewRequestAdapter extends RecyclerView.Adapter<NewRequestAdapter.ViewHolder> {

    private List<RequestWithId> listitems;
    private Context context;

    public NewRequestAdapter(List<RequestWithId> listitems, Context context) {
        this.listitems = listitems;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public static TextView requestID;
        public static TextView longitude;
        public static TextView latitude;
        public Button accept;
        public Button reject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestID = (TextView) itemView.findViewById(R.id.tvuser);
            longitude = (TextView) itemView.findViewById(R.id.tvlocation);
            latitude = (TextView) itemView.findViewById(R.id.tvlocation1);
            accept = (Button) itemView.findViewById(R.id.btnaccept);
            reject = (Button) itemView.findViewById(R.id.btnreject);

        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final RequestWithId request = listitems.get(position);
        holder.requestID.setText("Request id: " + request.getRequestId());
        holder.longitude.setText("Longitude: " + Double.toString(request.getLongitude()));
        holder.latitude.setText("Latitude: " + Double.toString(request.getLatitude()));


        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //    removeItem(position);
                sendData sD = new sendData(request.getRequestId(), request.getCitizen(), context);
                sD.execute();
            }
        });

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //    removeItem(position);
                rejectRequest rR = new rejectRequest(request.getRequestId(),context);
                rR.execute();
            }
        });

    }

    public void removeItem(int position){
        listitems.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listitems.size();
    }



   public class sendData extends AsyncTask<String, String, String> {

        private final String requestId;
        private final Context context;
       private final String citizen;

        sendData(String requestId, String citizen, Context context) {
            this.requestId = requestId;
            this.citizen=citizen;
            this.context=context;
        }

        @Override
        protected String doInBackground(String... strings) {
            String response;

            try {
                SessionUtil session = new SessionUtil(context);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("requestId", requestId);
                jsonObject.put("acceptedBy", session.getUsername());
                jsonObject.put("citizen", citizen);
                Log.d("The citizen is ", citizen);
                HttpConnection httpConnection = new HttpConnection();

                Log.d("Data is",jsonObject.toString());

                response = httpConnection.doPostRequest("serviceProvider/acceptRequest", jsonObject);
                Log.d("Accepted by Service", "Data sent to Server: " + response);

                if (response == null)
                    return null;

                return response;


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                Log.d("Service sending data", "Some error has occurred at Server");
                //   Toast.makeText(MainActivity.this, "Some error occured. Try again", Toast.LENGTH_LONG).show();
            }


        }

        @Override
        protected void onCancelled() {
            Log.d("LoginActivity", "Login Task Cancelled");
        }

    }


    public class rejectRequest extends AsyncTask<String, String, String> {

        private final String requestId;
        private final Context context;

        rejectRequest(String requestId, Context context) {
            this.requestId = requestId;
            this.context=context;
        }

        @Override
        protected String doInBackground(String... strings) {
            String response;

            try {
                SessionUtil session = new SessionUtil(context);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("requestId", requestId);
                jsonObject.put("acceptedBy", session.getUsername());
                HttpConnection httpConnection = new HttpConnection();

                Log.d("Data is",jsonObject.toString());

                response = httpConnection.doPostRequest("serviceProvider/rejectRequest", jsonObject);
                Log.d("Accepted by Service", "Data sent to Server: " + response);

                if (response == null)
                    return null;

                return response;


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                Log.d("Service sending data", "Some error has occurred at Server");
                //   Toast.makeText(MainActivity.this, "Some error occured. Try again", Toast.LENGTH_LONG).show();
            }


        }

        @Override
        protected void onCancelled() {
            Log.d("LoginActivity", "Login Task Cancelled");
        }

    }
}
