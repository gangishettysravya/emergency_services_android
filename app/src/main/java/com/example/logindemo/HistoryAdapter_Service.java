package com.example.logindemo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryAdapter_Service extends RecyclerView.Adapter<HistoryAdapter_Service.ViewHolder> {
    private List<RequestWithId> listitems;
    private Context context;

    public HistoryAdapter_Service(List<RequestWithId> listitems, Context context) {
        this.listitems = listitems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_layout, parent, false);
        return new HistoryAdapter_Service.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryAdapter_Service.ViewHolder holder, final int position) {

        final RequestWithId request = listitems.get(position);
        holder.requestId.setText("Request id " + request.getRequestId());
        holder.location.setText("Location : " + Double.toString(request.getLatitude()) + "," + Double.toString(request.getLongitude()));
        holder.status.setText("Status : " + request.getStatus());

        if(request.getStatus().equals("Completed")){
            holder.completed.setVisibility(View.INVISIBLE);
        }

        else{

            holder.completed.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){

                    completeRequest cR = new completeRequest(request.getRequestId(),request.getCitizen(),context,request.getExpert());
                    cR.execute();
                    holder.completed.setVisibility(View.INVISIBLE);
                    /*
                    holder.status.setText("Status : " + "Completed");
                    listitems.get(position).setStatus("Completed");
                    notifyDataSetChanged();
                    */
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return listitems.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public static TextView requestId;
        public static TextView location;
        public static TextView status;
        public Button completed;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestId = (TextView) itemView.findViewById(R.id.tvUserreqId);
            location = (TextView) itemView.findViewById(R.id.tvuserLocation);
            status = (TextView) itemView.findViewById(R.id.tvstatus);
            completed = (Button) itemView.findViewById(R.id.completed_btn);
        }
    }

    public class completeRequest extends AsyncTask<String, String, String> {

        private final String requestId;
        private final String citizen;
        private final Context context;
        private final String expert;

        completeRequest(String requestId,String citizen,Context context,String expert) {
            this.requestId = requestId;
            this.context=context;
            this.citizen = citizen;
            this.expert = expert;
        }

        @Override
        protected String doInBackground(String... strings) {
            String response;

            try {
                SessionUtil session = new SessionUtil(context);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("requestId", requestId);
                jsonObject.put("acceptedBy", session.getUsername());
                jsonObject.put("citizen",citizen);
                jsonObject.put("expert",expert);
                HttpConnection httpConnection = new HttpConnection();

                Log.d("Data is",jsonObject.toString());

                response = httpConnection.doPostRequest("serviceProvider/completedRequest", jsonObject);
                Log.d("Completed by Service", "Data sent to Server: " + response);

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