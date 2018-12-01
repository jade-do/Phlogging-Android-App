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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phlogging);

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
        listAdapter = new SimpleAdapter(this, fetchAllPhlogEntries(), R.layout.list_item);
        theList.setAdapter(listAdapter);
    }

    // Mapping the DB to the ListItems ArrayList
    private ArrayList<HashMap<String,Object>> fetchAllPhlogEntries(){
        List<DataRoomEntity> dbEntities;
        HashMap<String,Object> oneItem;
        ArrayList<HashMap<String,Object>> listItems;
        Time theTime;
        long unixTime;

        dbEntities = phlogEntryDB.daoAccess().fetchAllPhlogs();
        listItems = new ArrayList<>();

        theTime = new Time();

        for (DataRoomEntity onePhlog: dbEntities) {
            oneItem = new HashMap<>();
            oneItem.put("phlog_photo", Uri.parse(onePhlog.getCameraPhotoUriString()));
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

    // Opens a second activity to edit the description upon a long click on one of the view
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long rowId){
        Intent editPhlogEntry = new Intent();

        editPhlogEntry.setClassName("edu.miami.cs.jadedo.phlogging", "edu.miami.cs.jadedo.phlogging.EditPhlogEntry");
        startActivityForResult(editPhlogEntry, ACTIVITY_EDIT_PHLOG_ENTRY);

        return true;
    }

}
