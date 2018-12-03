package edu.miami.cs.jadedo.phlogging;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.arch.persistence.room.Room;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.net.Uri;
import android.text.format.Time;
import android.view.View;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.Toast;
import android.app.Activity;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class Phlogging extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String DATABASE_NAME = "PhlogEntry.db";
    private DataRoomDB phlogEntryDB;
    private List<DataRoomEntity> dbEntities;
    private ListView theList;
    SimpleAdapter listAdapter;
    private final int ACTIVITY_EDIT_PHLOG_ENTRY = 1;
    private final int ACTIVITY_VIEW_PHLOG_ENTRY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phlogging);

        fillList();
    }

    public void fillList(){
        String[] displayFields = {
                "phlog_photo",
                "phlog_title",
                "phlog_description",
                "phlog_time"
        };

        int[] displayViews = {
                R.id.phlog_photo,
                R.id.phlog_title,
                R.id.phlog_description,
                R.id.phlog_time
        };
        // Starting the Database:
        phlogEntryDB = Room.databaseBuilder(getApplicationContext(), DataRoomDB.class, DATABASE_NAME).allowMainThreadQueries().build();

        // Mapping the DB to the ListView
        theList = findViewById(R.id.the_list);
        listAdapter = new SimpleAdapter(this, fetchAllPhlogEntries(), R.layout.list_item, displayFields, displayViews);
        theList.setAdapter(listAdapter);
        theList.setOnItemClickListener(this);
        theList.setOnItemLongClickListener(this);
    }

    // Mapping the DB to the ListItems ArrayList
    private ArrayList<HashMap<String,Object>> fetchAllPhlogEntries(){
        HashMap<String,Object> oneItem;
        ArrayList<HashMap<String,Object>> listItems;
        Time theTime;
        long unixTime;

        dbEntities = phlogEntryDB.daoAccess().fetchAllPhlogs();
        listItems = new ArrayList<>();

        theTime = new Time();

        for (DataRoomEntity onePhlog: dbEntities) {
            oneItem = new HashMap<>();
            if (onePhlog.getCameraPhotoUriString() != null) {
                oneItem.put("phlog_photo", Uri.parse(onePhlog.getCameraPhotoUriString()));
            }
            oneItem.put("phlog_title", onePhlog.getPhlogTitle());
            oneItem.put("phlog_description", onePhlog.getText());
            unixTime = onePhlog.getUnixTime();
            theTime.set(unixTime);
            oneItem.put("phlog_time", theTime.format("%A %D %T"));
            listItems.add(oneItem);
        }

        return (listItems);
    }

    public void myClickHandler(View view){

        Intent editPhlogEntry;

        switch(view.getId()) {

            // Starting the EditPhlogEntry activity:
            case R.id.add_entry_button:
                editPhlogEntry = new Intent();
                editPhlogEntry.setClassName("edu.miami.cs.jadedo.phlogging", "edu.miami.cs.jadedo.phlogging.EditPhlogEntry");
                startActivityForResult(editPhlogEntry, ACTIVITY_EDIT_PHLOG_ENTRY);
                break;
            default:
                break;
        }
    }

    // View a phlog entry and enables
    public void onItemClick (AdapterView<?> parent, View view, int position, long rowId){
        long unixTime;
        Intent viewPhlogEntry = new Intent();

        unixTime = dbEntities.get(position).getUnixTime();
        viewPhlogEntry.setClassName("edu.miami.cs.jadedo.phlogging", "edu.miami.cs.jadedo.phlogging.ViewPhlogEntry");
        viewPhlogEntry.putExtra("unix_time", unixTime);
        startActivityForResult(viewPhlogEntry, ACTIVITY_VIEW_PHLOG_ENTRY);

    }

    // Edit Phlog Entry activ
    public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long rowId) {
        long unixTime;
        Intent editPhlogEntry;

        unixTime = dbEntities.get(position).getUnixTime();
        editPhlogEntry = new Intent();
        editPhlogEntry.setClassName("edu.miami.cs.jadedo.phlogging", "edu.miami.cs.jadedo.phlogging.EditPhlogEntry");
        editPhlogEntry.putExtra("unix_time", unixTime);
        startActivityForResult(editPhlogEntry, ACTIVITY_EDIT_PHLOG_ENTRY);

        return true;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        long deleteUnixTime;
        DataRoomEntity deletePhlogEntry;

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVITY_EDIT_PHLOG_ENTRY:
                if (resultCode == Activity.RESULT_OK) {
                    fillList();
                } else {
                    Toast.makeText(this, "Okay, return to main activity",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case ACTIVITY_VIEW_PHLOG_ENTRY:
                if (resultCode == Activity.RESULT_OK){
                    deleteUnixTime = data.getLongExtra("edu.miami.cs.jadedo.phlogging.delete_unix_time", -1);
                    if (deleteUnixTime != -1){
                        deletePhlogEntry = phlogEntryDB.daoAccess().getPhlogByUnixTime(deleteUnixTime);
                        phlogEntryDB.daoAccess().deletePhlog(deletePhlogEntry);
                        fillList();
                    }
                }
                break;
            default:
                break;

        }

    }

}
