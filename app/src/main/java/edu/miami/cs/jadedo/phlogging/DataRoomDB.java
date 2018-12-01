package edu.miami.cs.jadedo.phlogging;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database (entities = {DataRoomEntity.class}, version = 1, exportSchema = false)
public abstract class DataRoomDB extends RoomDatabase{
    public abstract DataRoomAccess daoAccess();
}
