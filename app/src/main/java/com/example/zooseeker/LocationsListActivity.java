package com.example.zooseeker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

public class LocationsListActivity extends AppCompatActivity {
    public RecyclerView recyclerView;

    private LocationsListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_list);

        LocationsListViewModel viewModel = new ViewModelProvider(this).get(LocationsListViewModel.class);
        this.viewModel = viewModel;

        LocationsListAdapter adapter = new LocationsListAdapter();
        adapter.setHasStableIds(true);
        adapter.setOnDeleteClickedHandler(viewModel::deleteLocation);
        viewModel.getLocationsListItems().observe(this, adapter::setLocationsListItems);

        recyclerView = findViewById(R.id.locations_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //adapter.setLocationsListItems(LocationsListItem.loadJSON(this, "sample_zoo_graph.json"));

    }

    public void onClosedClicked(View view) {
        finish();
    }
}