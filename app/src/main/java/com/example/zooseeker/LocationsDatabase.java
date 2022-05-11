package com.example.zooseeker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Database(entities={LocationsListItem.class}, version = 3)
public abstract class LocationsDatabase extends RoomDatabase {
    private static LocationsDatabase singleton = null;

    public abstract LocationsListItemDao locationsListItemDao();

    public synchronized static LocationsDatabase getSingleton(Context context) {
        if (singleton == null) {
            singleton = LocationsDatabase.makeDatabase(context);
        }
        return singleton;
    }

    @VisibleForTesting
    public static void injectTestDatabase(LocationsDatabase testDatabase) {
        if (singleton != null) {
            singleton.close();
        }
        singleton = testDatabase;
    }

    private static LocationsDatabase makeDatabase(Context context) {
        return Room.databaseBuilder(context, LocationsDatabase.class, "locations_app.db")
                .allowMainThreadQueries()
                .build();
    }
}
