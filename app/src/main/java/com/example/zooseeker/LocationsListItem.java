package com.example.zooseeker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jgrapht.Graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
        Graph<String, IdentifiedWeightedEdge> graph =
                ZooData.loadZooGraphJSON(context, path);

        List<LocationsListItem> vertexList = new ArrayList<LocationsListItem>();
        Set<String> graphVertices = graph.vertexSet();
        int order = 1;

        for (String vertex : graphVertices) {
            LocationsListItem item = new LocationsListItem(vertex, 0, order++);
            vertexList.add(item);
            Log.d("Added Vertex", vertex);
        }
        return vertexList;
    }
}
