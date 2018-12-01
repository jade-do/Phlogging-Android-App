package edu.miami.cs.jadedo.phlogging;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "PhlogEntry")

public class DataRoomEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "phlog_title")
    private String phlogTitle;
    @ColumnInfo(name = "unix_time")
    private long unixTime;
    @ColumnInfo(name = "text")
    private String text;
    @ColumnInfo(name = "camera_photo_uri_string")
    private String cameraPhotoUriString;
    @ColumnInfo(name = "gallery_photo_uri_string")
    private String galleryPhotoUriString;
    @ColumnInfo(name = "location_latitude")
    private String locationLatitude;
    @ColumnInfo(name = "location_longitude")
    private String locationLongitude;
    @ColumnInfo(name = "geodecoded_location")
    private String geodecodedLocation;
    @ColumnInfo(name = "orientation")
    private String orientation;
    @ColumnInfo(name = "contact_uri")
    private String contactUri;
    @ColumnInfo(name = "light_reading")
    private String lightReading;
    //-----------------------------------------------
    public DataRoomEntity() {
    }

    public int getId(){
        return (id);
    }

    public String getPhlogTitle(){
        return (phlogTitle);
    }

    public long getUnixTime(){
        return (unixTime);
    }

    public String getText(){
        return text;
    }

    public String getCameraPhotoUriString(){
        return cameraPhotoUriString;
    }

    public String getGalleryPhotoUriString(){
        return galleryPhotoUriString;
    }

    public String getLocationLatitude(){
        return locationLatitude;
    }

    public String getLocationLongitude(){
        return locationLongitude;
    }

    public String getGeodecodedLocation(){
        return geodecodedLocation;
    }

    public String getOrientation(){
        return orientation;
    }

    public String getContactUri() {
        return contactUri;
    }

    public String getLightReading(){
        return lightReading;
    }

    public void setId(int newId){
        id = newId;
    }

    public void setPhlogTitle(String newPhlogTitle){
        phlogTitle = newPhlogTitle;
    }

    public void setUnixTime(long newUnixTime){
        unixTime = newUnixTime;
    }

    public void setText(String newText){
        text = newText;
    }

    public void setCameraPhotoUriString(String newCameraPhotoUriString){
        cameraPhotoUriString = newCameraPhotoUriString;
    }

    public void setGalleryPhotoUriString(String newGalleryPhotoUriString){
        galleryPhotoUriString = newGalleryPhotoUriString;
    }

    public void setLocationLatitude(String newLocationLatitude){
        locationLatitude = newLocationLatitude;
    }

    public void setLocationLongitude(String newLocationLongitude){
        locationLongitude = newLocationLongitude;
    }

    public void setGeodecodedLocation(String newGeodecodedLocation){
        geodecodedLocation = newGeodecodedLocation;
    }

    public void setOrientation(String newOrientation){
        orientation = newOrientation;
    }

    public void setContactUri(String newContactUri){
        contactUri = newContactUri;
    }

    public void setLightReading(String newLightReading){
        lightReading = newLightReading;
    }
}
