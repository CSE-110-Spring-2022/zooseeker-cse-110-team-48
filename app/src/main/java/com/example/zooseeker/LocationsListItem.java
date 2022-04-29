package com.example.zooseeker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@Entity(tableName = "locations_list_items")
public class LocationsListItem {
    @PrimaryKey(autoGenerate = true)
    public long id = 0;
    @NonNull
    public String text;
    public int order;
    public double distance;

    LocationsListItem(@NonNull String text, double distance, int order) {
        this.text = text;
        this.distance = distance;
        this.order = order;
    }

    @Override
    public String toString() {
        return "LocationsListItem{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", order=" + order +
                ", distance=" + distance +
                '}';
    }

    public static List<LocationsListItem> loadJSON(Context context, String path) {
        try {
            InputStream input = context.getAssets().open(path);
            Reader reader = new InputStreamReader(input);
            Gson gson = new Gson();
            Type type = new TypeToken<List<LocationsListItem>>(){}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
