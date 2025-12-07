package com.filantrop.androidworkmanagerexample.sni;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SniDto.class}, version = 1, exportSchema = false)
public abstract class SniDatabase extends RoomDatabase {
    public abstract SniDao sniDao();

    private static SniDatabase instance;

    public static synchronized SniDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), SniDatabase.class, "SniDatabase")
                    .fallbackToDestructiveMigration(true)
                    .build();
        }
        return instance;
    }

}
