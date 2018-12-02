package edu.miami.cs.jadedo.phlogging;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.TextView;

import java.util.List;

public class SensorLocatorDecoder extends AsyncTask<Location, Void, String>{
    private Context theContext;
    private Activity theActivity;

    public SensorLocatorDecoder(Context context, Activity activity){
        theContext = context;
        theActivity = activity;
    }

    protected String doInBackground(Location... location){
        return(androidGeodecode(location[0]));
    }

    protected void onPostExecute(String result){
        ((TextView) theActivity.findViewById(R.id.edit_location)).setText(result);
    }

    private String androidGeodecode(Location thisLocation){
        Geocoder androidGeocoder;
        List<Address> addresses;
        Address firstAddress;
        String addressLine;
        String locationName;
        int index;

        if (Geocoder.isPresent()){
            androidGeocoder = new Geocoder(theContext);
            try {
                addresses = androidGeocoder.getFromLocation(thisLocation.getLatitude(), thisLocation.getLongitude(), 1);
                if (addresses.isEmpty()){
                    return("ERROR: Unknown location");
                } else {
                    firstAddress = addresses.get(0);
                    locationName = "";
                    index = 0;
                    while((addressLine = firstAddress.getAddressLine(index)) != null){
                        locationName += addressLine + ", ";
                        index++;
                    }
                    return locationName;
                }
            } catch (Exception e) {
                return("ERROR: " + e.getMessage());
            }
        } else {
            return ("ERROR: No geocoder available");
        }
    }

}
