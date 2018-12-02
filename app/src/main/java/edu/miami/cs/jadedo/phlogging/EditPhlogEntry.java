package edu.miami.cs.jadedo.phlogging;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.arch.persistence.room.Room;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.net.Uri;
import android.view.View;
import android.os.Environment;
import android.text.format.Time;
import android.hardware.Camera;
import android.content.Intent;
import android.provider.MediaStore;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import android.location.Location;
import android.os.Looper;
import android.location.LocationManager;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.File;
import java.util.List;

public class EditPhlogEntry extends AppCompatActivity  {

    private static final String DATABASE_NAME = "PhlogEntry.db";
    private DataRoomDB phlogEntryDB;
    private DataRoomEntity phlogEntry;
    private long unixTime;

    private static final int ACTIVITY_CAMERA_APP = 1;
    private static final boolean SAVE_TO_FILE = true;

    private Camera camera;
    private String cameraFileName;
    private ImageView cameraPhoto;

    private final int RESOLVE_SETTINGS = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private LocationRequest locationRequest;
    private LocationManager locationManager;
    private boolean gpsAvailable;
    private boolean networkAvailable;
    private boolean initial = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Time theTime = new Time();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_phlog_entry);

        // Set up the DB
        phlogEntryDB = Room.databaseBuilder(getApplicationContext(), DataRoomDB.class, DATABASE_NAME).allowMainThreadQueries().build();

        // Get time for data entry identification purposes
        unixTime = this.getIntent().getLongExtra("unix_time", 0);
        if (unixTime != 0) {
            phlogEntry = phlogEntryDB.daoAccess().getPhlogByUnixTime(unixTime);
        } else {
            phlogEntry = new DataRoomEntity();
        }

        setUpViewFromExistingData();

        // Setting up the camera
        theTime.set(unixTime);

        cameraFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                + theTime.format("%A %D %T")+ "-" + getString(R.string.camera_file_name);

        // Setting up the Location Provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(2000 / 2);
        locationManager = (LocationManager)(getSystemService(LOCATION_SERVICE));
        detectLocators();
        startLocating();
    }

    // Set up the view
    public void setUpViewFromExistingData(){
        EditText title;
        EditText text;
        ImageView galleryPhoto;
        TextView locationView;
        String location;

        title = findViewById(R.id.edit_title);
        text = findViewById(R.id.edit_text);
        cameraPhoto = findViewById(R.id.edit_camera_photo);
        galleryPhoto = findViewById(R.id.edit_gallery_photo);
        locationView = findViewById(R.id.edit_location);

        if (phlogEntry.getPhlogTitle() != null) {
            title.setText(phlogEntry.getPhlogTitle());
        } else {
            title.setText("Enter A Title For This Phlog");
        }
        if (phlogEntry.getText() != null) {
            text.setText(phlogEntry.getText());
        } else {
            text.setText("Enter A Description For This Phlog");
        }
        if (phlogEntry.getCameraPhotoUriString() != null) {
            cameraPhoto.setImageURI(Uri.parse(phlogEntry.getCameraPhotoUriString()));
        }
        if (phlogEntry.getGalleryPhotoUriString() != null) {
            galleryPhoto.setImageURI(Uri.parse(phlogEntry.getGalleryPhotoUriString()));
        }

        location = "";
        //if (phlogEntry.getLocationLatitude() != null) {
            location += phlogEntry.getLocationLatitude() + " ";
        //}
        //if (phlogEntry.getLocationLongitude() != null){
            location += phlogEntry.getLocationLongitude() + " ";
        //}
        if (phlogEntry.getGeodecodedLocation() != null){
            location += "\n" + phlogEntry.getGeodecodedLocation()+ " ";
        }
        if (phlogEntry.getOrientation() != null){
            location += "\n " + phlogEntry.getOrientation() + " ";
        }

        locationView.setText(location);

    }

    // Detect available locators for use
    public void detectLocators(){
        List<String> locators;

        locators = locationManager.getProviders(true);
        for (String aProvider : locators) {
            if (aProvider.equals(LocationManager.GPS_PROVIDER)) {
                gpsAvailable = true;
            } else {
                gpsAvailable = false;
            }
            if (aProvider.equals(LocationManager.NETWORK_PROVIDER)) {
                networkAvailable = true;
            } else {
                networkAvailable = false;
            }
        }
    }

    // Start receving location updates every 2s
    private void startLocating(){
        if (gpsAvailable) {
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            Toast.makeText(this,"Using GPS Locator",Toast.LENGTH_SHORT).show();
        } else if (networkAvailable){
            locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
            Toast.makeText(this,"Using Network Locator",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"No Locator Available",Toast.LENGTH_SHORT).show();
        }
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, myLocationCallback, Looper.myLooper());
        } catch(SecurityException e) {
            Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
        }
    }

    LocationCallback myLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult){
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    public void onLocationChanged(Location newLocation){
        Toast.makeText(this, "In onLocationChanged", Toast.LENGTH_LONG).show();
        Log.i("IN", "onLocationChanged");
        if (newLocation == null) {
            Toast.makeText(this,"No location",Toast.LENGTH_SHORT).show();
            return;
        }

        // If first time, set current location to new location
        if (initial){
            currentLocation = newLocation;
            initial = false;
            Toast.makeText(this,"New location updated",Toast.LENGTH_SHORT).show();
            new SensorLocatorDecoder(getApplicationContext(), this).execute(currentLocation);

            // If not first time, only update if moved 100 m from previous location
        } else if (currentLocation != null && currentLocation.distanceTo(newLocation) > getResources().getInteger(R.integer.threshold_for_last_location_meter)){
            currentLocation = newLocation;
            Toast.makeText(this,"New location updated",Toast.LENGTH_SHORT).show();
            new SensorLocatorDecoder(getApplicationContext(), this).execute(currentLocation);

        }

    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(myLocationCallback);
    }

    public void myClickHandler(View view) {

        Intent cameraIntent;
        String geodecodedLocation;

        switch (view.getId()) {
            case R.id.edit_take_photo_button:
                // Opens the camera app and take photo from there
                cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cameraFileName)));
                startActivityForResult(cameraIntent, ACTIVITY_CAMERA_APP);
                break;
            case R.id.edit_update_location:
                // Update the location section when user clicks Update Location Button
                new SensorLocatorDecoder(getApplicationContext(), this).execute(currentLocation);
                geodecodedLocation = ((TextView) findViewById(R.id.edit_location)).getText().toString();
                phlogEntry.setLocationLatitude(currentLocation.getLatitude());
                phlogEntry.setLocationLongitude(currentLocation.getLongitude());
                phlogEntry.setGeodecodedLocation(geodecodedLocation);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode,resultCode,returnedIntent);
        Uri cameraPhotoUri;
        String cameraPhotoUriString;

        switch (requestCode) {
            case ACTIVITY_CAMERA_APP:
                if (resultCode == Activity.RESULT_OK) {
                    cameraPhotoUri = Uri.fromFile(new File(cameraFileName));
                    cameraPhoto.setImageURI(cameraPhotoUri);
                    cameraPhotoUriString = cameraPhotoUri.toString();
                    phlogEntry.setCameraPhotoUriString(cameraPhotoUriString);
                }
                break;
            default:
                break;
        }
    }

}