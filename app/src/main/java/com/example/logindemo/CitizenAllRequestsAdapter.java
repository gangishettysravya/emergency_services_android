package com.example.logindemo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class CitizenAllRequestsAdapter extends ArrayAdapter<RequestWithId>{

    private Context context;
    private List<RequestWithId> Allrequests;

    public CitizenAllRequestsAdapter(Context context, int resource, List<RequestWithId> objects) {

        super(context, resource, objects);
        this.context = context;
        this.Allrequests = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        //get the property we are displaying

        View view;
        final RequestWithId request = Allrequests.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.citizen_single_request,null);
        final Button chat = (Button) view.findViewById(R.id.chat_btn);
        if(request.getStatus().equals("Completed")){
            chat.setVisibility(View.INVISIBLE);
        }
        else{
            chat.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    //TODO THIS
                }
            });

        }

        TextView requestId = (TextView) view.findViewById(R.id.requestId);
        TextView description = (TextView) view.findViewById(R.id.description);
        TextView category = (TextView) view.findViewById(R.id.serviceCategory);
        TextView location = (TextView) view.findViewById(R.id.location);
        TextView status = (TextView) view.findViewById(R.id.status);
        TextView accepted_by = (TextView) view.findViewById(R.id.acceptedBy);

        requestId.setText("Request No " +request.getRequestId());
        description.setText("Description : " + request.getDescription());
        category.setText("Category : " + request.getServiceCategory());
        status.setText("Status : " + request.getStatus());
        accepted_by.setText("Accepted By : " + request.getAcceptedBy());
        location.setText("Location : "+ Double.toString(request.getLatitude())+","+Double.toString(request.getLongitude()));

        return view;
    }
}