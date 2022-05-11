package com.example.zooseeker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jgrapht.Graph;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity(tableName = "locations_list_items")
public class LocationsListItem {
    @PrimaryKey(autoGenerate = true)
    public long id = 0;
    @NonNull
    public String text;
    public String textId;
    public int order;
    public double distance;

    LocationsListItem(@NonNull String text, @NonNull String textId, double distance, int order) {
        this.text = text;
        this.distance = distance;
        this.order = order;
        this.textId = textId;
    }

    @Override
    public String toString() {
        return "LocationsListItem{" +
                "id=" + id +
                ", text=" + text +
                ", textId=" + textId +
                ", order=" + order +
                ", distance=" + distance +
                '}';
    }
}
