package com.example.zooseeker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

public class LocationsListActivity extends AppCompatActivity {
    public RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_list);

        LocationsListAdapter adapter = new LocationsListAdapter();
        adapter.setHasStableIds(true);

        recyclerView = findViewById(R.id.locations_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setLocationsListItems(LocationsListItem.loadJSON(this, "sample_zoo_graph.json"));

    }

    public void onClosedClicked(View view) {
        finish();
    }
}