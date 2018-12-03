package edu.miami.cs.jadedo.phlogging;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.arch.persistence.room.Room;
import android.view.Display;
import android.view.WindowManager;
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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

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

public class EditPhlogEntry extends AppCompatActivity implements SensorEventListener {

    private static final String DATABASE_NAME = "PhlogEntry.db";
    private DataRoomDB phlogEntryDB;
    private DataRoomEntity phlogEntry;
    private long unixTime;

    private static final int ACTIVITY_CAMERA_APP = 1;
    private static final int ACTIVITY_SELECT_PICTURE = 2;
    private static final boolean SAVE_TO_FILE = true;

    private Camera camera;
    private String cameraFileName;
    private ImageView cameraPhoto;
    private ImageView galleryPhoto;
    private String cameraPhotoUriString;
    private String galleryPhotoUriString;

    private final int RESOLVE_SETTINGS = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private LocationRequest locationRequest;
    private LocationManager locationManager;
    private boolean gpsAvailable;
    private boolean networkAvailable;
    private boolean initial = true;

    private float[] magneticField = new float[3];
    private boolean magneticFieldAvailable;
    private float[] gravity = new float[3];
    private boolean gravityAvailable;
    private float[] orientation = new float[3];
    private boolean orientationAvailable;
    private float[] light = new float[1];
    private boolean lightAvailable;

    private SensorManager sensorManager;
    private Display screen;

    private String sensorValues = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {



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

        cameraFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                + "Photo-" + unixTime + ".jpeg";

        // Setting up the Location Provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(getResources().getInteger(
                R.integer.time_between_location_updates_ms));
        locationRequest.setFastestInterval(getResources().getInteger(
                R.integer.time_between_location_updates_ms) / 2);
        locationManager = (LocationManager)(getSystemService(LOCATION_SERVICE));
        detectLocators();
        startLocating();

        // Setting up the Sensor Locator
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        screen = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        magneticFieldAvailable = startSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravityAvailable= startSensor(Sensor.TYPE_ACCELEROMETER);
        orientationAvailable = magneticFieldAvailable && gravityAvailable;
        lightAvailable = startSensor(Sensor.TYPE_LIGHT);
    }

    // Set up the view
    public void setUpViewFromExistingData(){
        EditText title;
        EditText text;
        ImageView galleryPhoto;
        TextView locationView;
        TextView sensorValuesView;
        String location;

        title = findViewById(R.id.edit_title);
        text = findViewById(R.id.edit_text);
        cameraPhoto = findViewById(R.id.edit_camera_photo);
        galleryPhoto = findViewById(R.id.edit_gallery_photo);
        locationView = findViewById(R.id.edit_location);
        sensorValuesView = findViewById(R.id.edit_sensor);

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
            cameraPhotoUriString = phlogEntry.getCameraPhotoUriString();
            cameraPhoto.setImageURI(Uri.parse(cameraPhotoUriString));
        }
        if (phlogEntry.getGalleryPhotoUriString() != null) {
            galleryPhotoUriString = phlogEntry.getGalleryPhotoUriString();
            galleryPhoto.setImageURI(Uri.parse(galleryPhotoUriString));
        }

        if (phlogEntry.getGeodecodedLocation() != null){
            location = phlogEntry.getGeodecodedLocation()+ " ";
            locationView.setText(location);
        }

        if (phlogEntry.getSensorValues() != null){
            sensorValuesView.setText(phlogEntry.getSensorValues());
        }

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
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    myLocationCallback,Looper.myLooper());
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
        if (newLocation == null) {
            Toast.makeText(this,"No location",Toast.LENGTH_SHORT).show();
            return;
        }

        // If first time, set current location to new location
        if (initial){
            currentLocation = newLocation;
            new SensorLocatorDecoder(getApplicationContext(), this).execute(newLocation);

            initial = false;

            // If not first time, only update if moved 1000 m from previous location
        } else if (currentLocation != null && currentLocation.distanceTo(newLocation) > getResources().getInteger(R.integer.threshold_for_last_location_meter)){
            currentLocation = newLocation;
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
        sensorManager.unregisterListener(this);
    }

    public void myClickHandler(View view) {

        Intent cameraIntent;
        Intent returnIntent;
        String geodecodedLocation;
        EditText title;
        EditText text;

        switch (view.getId()) {
            case R.id.edit_take_photo_button:
                // Opens the camera app and take photo from there
                cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cameraFileName)));
                startActivityForResult(cameraIntent, ACTIVITY_CAMERA_APP);
                break;
            //case R.id.edit_update_location:
                // Update the location section when user clicks Update Location Button
                //new SensorLocatorDecoder(getApplicationContext(), this).execute(currentLocation);
                //break;
            case R.id.edit_select_photo_button:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, ACTIVITY_SELECT_PICTURE);
                break;
            case R.id.edit_update_location_button:
                new SensorLocatorDecoder(getApplicationContext(), this).execute(currentLocation);
                break;
            case R.id.edit_save_button:

                title = (EditText) findViewById(R.id.edit_title);
                text = (EditText) findViewById(R.id.edit_text);

                geodecodedLocation = ((TextView) findViewById(R.id.edit_location)).getText().toString();
                phlogEntry.setPhlogTitle(title.getText().toString());
                phlogEntry.setText(text.getText().toString());
                phlogEntry.setCameraPhotoUriString(cameraPhotoUriString);
                phlogEntry.setGalleryPhotoUriString(galleryPhotoUriString);
                phlogEntry.setLocationLatitude(currentLocation.getLatitude());
                phlogEntry.setLocationLongitude(currentLocation.getLongitude());
                phlogEntry.setGeodecodedLocation(geodecodedLocation);
                phlogEntry.setSensorValues(sensorValues);

                // Only update new time if this is a newly created phlog
                if (unixTime == 0){
                    phlogEntry.setUnixTime(System.currentTimeMillis());
                    phlogEntryDB.daoAccess().addPhlog(phlogEntry);

                } else {
                    phlogEntryDB.daoAccess().updatePhlog(phlogEntry);
                }

                returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode,resultCode,returnedIntent);
        Uri photoUri;

        switch (requestCode) {
            case ACTIVITY_CAMERA_APP:
                if (resultCode == Activity.RESULT_OK) {
                    photoUri = Uri.fromFile(new File(cameraFileName));
                    cameraPhoto = findViewById(R.id.edit_camera_photo);
                    cameraPhoto.setImageURI(photoUri);
                    cameraPhotoUriString = photoUri.toString();
                }
                break;
            case ACTIVITY_SELECT_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    photoUri = returnedIntent.getData();

                    if (photoUri == null) {
                        finish();
                    } else {
                        galleryPhotoUriString = photoUri.toString();
                        galleryPhoto = findViewById(R.id.edit_gallery_photo);
                        galleryPhoto.setImageURI(photoUri);
                    }
                }
                break;
            default:
                break;
        }
    }

    private boolean startSensor(int sensorType){
        if (sensorManager.getSensorList(sensorType).isEmpty()){
            return false;
        } else {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(sensorType), SensorManager.SENSOR_DELAY_NORMAL);
            return true;
        }
    }


    // Detect any changes in orientation
    public void onSensorChanged (SensorEvent event) {
        boolean gravityChanged, magneticFieldChanged, orientationChanged, lightChanged;

        float R[] = new float[9];
        float I[] = new float[9];
        float newOrientation[] = new float[3];

        gravityChanged = magneticFieldChanged = orientationChanged = lightChanged = false;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                gravityChanged = arrayCopyChangeTest(event.values, gravity, 1.0f);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticFieldChanged = arrayCopyChangeTest(event.values, magneticField, 1.0f);
                break;
            case Sensor.TYPE_LIGHT:
                lightChanged = arrayCopyChangeTest(event.values,light,1.0f);
                break;
            default:
                break;
        }

        if ((gravityChanged || magneticFieldChanged) && SensorManager.getRotationMatrix(R,I, gravity, magneticField)){
            SensorManager.getOrientation(R, newOrientation);
            newOrientation[0] = (float)Math.toDegrees(newOrientation[0]);
            newOrientation[1] = (float)Math.toDegrees(newOrientation[1]);
            newOrientation[2] = (float)Math.toDegrees(newOrientation[2]);
            orientationChanged = arrayCopyChangeTest(newOrientation, orientation, 5.0f); //---- 5 degrees
        }

        if (orientationChanged || gravityChanged || lightChanged) {
            updateSensorDisplay();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }

    private boolean arrayCopyChangeTest(float[] from, float[] to, float amountForChange){
        int copyIndex;
        boolean changed = false;

        for (copyIndex = 0; copyIndex < to.length; copyIndex++){
            if (Math.abs(from[copyIndex] - to[copyIndex]) > amountForChange){
                to[copyIndex] = from[copyIndex];
                changed = true;
            }
        }

        return (changed);
    }


    private void updateSensorDisplay() {

        final String format = "%5.1f";

        sensorValues = "";
        sensorValues += "Orientation\n";
        if (orientationAvailable) {
            sensorValues +=
                    "A " + String.format(format, orientation[0]) + ", " +
                            "P " + String.format(format, orientation[1]) + ", " +
                            "R " + String.format(format, orientation[2]) + "\n\n";
        } else {
            sensorValues += "Not available\n\n";
        }

        sensorValues += "Gravity\n";
        if (gravityAvailable) {
            sensorValues +=
                    "X " + String.format(format, gravity[0]) + "," +
                            "Y " + String.format(format, gravity[1]) + "," +
                            "Z " + String.format(format, gravity[2]) + "\n\n";
        } else {
            sensorValues += "Not available\n\n";
        }


        sensorValues += "Light\n";
        if (lightAvailable) {
            sensorValues +=
                    "L " + String.format(format, light[0]) + "\n\n";
        } else {
            sensorValues += "Not available\n\n";
        }

        ((TextView) findViewById(R.id.edit_sensor)).setText(sensorValues);
    }
}