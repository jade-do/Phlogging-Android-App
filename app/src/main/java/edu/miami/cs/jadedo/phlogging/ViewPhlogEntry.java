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
import android.provider.ContactsContract;
import android.app.Activity;
import android.database.Cursor;
import android.util.Log;

import org.w3c.dom.Text;

public class ViewPhlogEntry extends AppCompatActivity {

    private static final String DATABASE_NAME = "PhlogEntry.db";
    private DataRoomDB phlogEntryDB;
    private DataRoomEntity phlogEntry;
    private long unixTime;
    private final int ACTIVITY_SELECT_CONTACT = 1;
    private static final int ACTIVITY_SEND_EMAIL = 2;

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
        Intent contactIntent;

        switch(view.getId()){
            case R.id.share_button:
                // opens email
                // start contacts activity
                contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactIntent, ACTIVITY_SELECT_CONTACT);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Uri contactData;
        Cursor contactsCursor;
        String contactName;
        int contactId;
        String[] emailAddresses = new String[1];

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ACTIVITY_SELECT_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    contactData = data.getData();
                    contactsCursor = getContentResolver().query(contactData, null, null, null, null);
                    if (contactsCursor.moveToFirst()){
                        contactName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        contactId = contactsCursor.getInt(
                                contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                        contactsCursor.close();
                        emailAddresses[0] = searchForEmailAddressById(contactId);
                    }
                    else {
                        contactName = null;
                        emailAddresses[0] = null;
                        Log.i("IN onActivityResult", "No contact data found");
                    }
                    // start email activity
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddresses);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                            phlogEntryDB.daoAccess().getPhlogByUnixTime(unixTime).getPhlogTitle());
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, "
                            + contactName + ".  Here's an awesome photo I wanted to share with you. "
                            + phlogEntryDB.daoAccess().getPhlogByUnixTime(unixTime).getText());
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(phlogEntry.getCameraPhotoUriString()));
                    startActivityForResult(emailIntent, ACTIVITY_SEND_EMAIL);
                } else {
                    Log.i("IN onActivityResult", "Contact not selected");
                }
                break;
            case ACTIVITY_SEND_EMAIL:
                Toast.makeText(this, "Email Sent!", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }

    }

    public String searchForEmailAddressById(int contactId) {

        // want to fetch the contact id and the email address
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.DATA
        };

        Cursor emailCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,projection,"CONTACT_ID = ?",
                new String[]{Integer.toString(contactId)},null);

        // get email address
        String emailAddress;
        if (emailCursor.moveToFirst()) {
            emailAddress = emailCursor.getString(emailCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Email.DATA));
        } else {
            emailAddress = null;
            Log.i("IN searchForEmail", "No email address found");
        }
        emailCursor.close();

        return(emailAddress);

    }
}
