package edu.miami.cs.jadedo.phlogging;

import android.arch.persistence.room.Room;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.content.Intent;

import org.w3c.dom.Text;

public class ViewPhlogEntry extends AppCompatActivity {

    private static final String DATABASE_NAME = "PhlogEntry.db";
    private DataRoomDB phlogEntryDB;
    private DataRoomEntity phlogEntry;
    private long unixTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_phlog_entry);

        // Set up the DB
        phlogEntryDB = Room.databaseBuilder(getApplicationContext(), DataRoomDB.class, DATABASE_NAME).allowMainThreadQueries().build();

        // Get time for data entry identification purposes
        unixTime = this.getIntent().getLongExtra("unix_time", 0);
        if (unixTime == 0){
            Toast.makeText(this, "ERROR! Unix Time Not Found For ID Purposes", Toast.LENGTH_LONG).show();
            finish();
        } else {
            phlogEntry = phlogEntryDB.daoAccess().getPhlogByUnixTime(unixTime);
        }
        setUpViewFromExistingData();
    }

    public void setUpViewFromExistingData(){
        TextView title;
        TextView text;
        TextView locationView;
        TextView sensorView;
        String location;
        ImageView cameraPhoto;
        ImageView galleryPhoto;

        title = findViewById(R.id.view_title);
        text = findViewById(R.id.view_text);
        cameraPhoto = findViewById(R.id.view_camera_photo);
        galleryPhoto = findViewById(R.id.view_gallery_photo);
        locationView = findViewById(R.id.view_location);
        sensorView = findViewById(R.id.view_sensor_values);

        if (phlogEntry.getPhlogTitle() != null) {
            title.setText(phlogEntry.getPhlogTitle());
        }
        if (phlogEntry.getText() != null) {
            text.setText(phlogEntry.getText());
        }
        if (phlogEntry.getCameraPhotoUriString() != null) {
            cameraPhoto.setImageURI(Uri.parse(phlogEntry.getCameraPhotoUriString()));
        }
        if (phlogEntry.getGalleryPhotoUriString() != null) {
            galleryPhoto.setImageURI(Uri.parse(phlogEntry.getGalleryPhotoUriString()));
        }

        if (phlogEntry.getGeodecodedLocation() != null){
            location =  phlogEntry.getGeodecodedLocation()+ " ";
            locationView.setText(location);
        }
        if (phlogEntry.getSensorValues() != null){
            sensorView.setText(phlogEntry.getSensorValues() + " ");
        }
    }

    public void myClickHandler(View view){
        Intent returnIntent;

        switch(view.getId()){
            case R.id.share_button:

                break;
            case R.id.dismiss_button:
                returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                returnIntent.putExtra("edu.miami.cs.jadedo.phlogging.delete_unix_time", -1);
                finish();
                break;
            case R.id.delete_button:
                returnIntent = new Intent();
                returnIntent.putExtra("edu.miami.cs.jadedo.phlogging.delete_unix_time", unixTime);
                setResult(RESULT_OK, returnIntent);
                finish();
                break;
            default:
                break;
        }
    }
}
