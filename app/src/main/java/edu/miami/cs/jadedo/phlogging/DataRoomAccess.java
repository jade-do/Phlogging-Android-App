package edu.miami.cs.jadedo.phlogging;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DataRoomAccess {
    @Query("SELECT * FROM PhlogEntry ORDER BY unix_time DESC")
    List<DataRoomEntity> fetchAllPhlogs();

    @Query("SELECT * FROM PhlogEntry WHERE unix_time LIKE :time")
    DataRoomEntity getPhlogByUnixTime(long time);

    @Query("SELECT * FROM PhlogEntry WHERE phlog_title LIKE :title")
    DataRoomEntity getPhlogByTitle(String title);

    @Insert
    void addPhlog(DataRoomEntity newPhlog);

    @Delete
    void deletePhlog(DataRoomEntity oldPhlog);

    @Update
    void updatePhlog(DataRoomEntity oldPhlog);
}
