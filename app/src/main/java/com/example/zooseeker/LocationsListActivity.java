package com.example.zooseeker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocationsListActivity extends AppCompatActivity {
    public RecyclerView recyclerView;
    private LocationsListViewModel viewModel;

    // Paths to files
    public String graph_file;
    public String node_info_file;
    public String edge_info_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_list);
        Intent intent = getIntent();

        this.graph_file = intent.getExtras().getString("graph_file");
        this.node_info_file = intent.getExtras().getString("node_info_file");
        this.edge_info_file = intent.getExtras().getString("edge_info_file");

        LocationsListViewModel viewModel = new ViewModelProvider(this).get(LocationsListViewModel.class);
        this.viewModel = viewModel;

        LocationsListAdapter adapter = new LocationsListAdapter();
        adapter.setHasStableIds(true);
        adapter.setOnDeleteClickedHandler(viewModel::deleteLocation);
        viewModel.getLocationsListItems().observe(this, adapter::setLocationsListItems);
        Button planRouteButton = findViewById(R.id.plan_route_btn);
        planRouteButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        launchRoutePlan(v);
                    }
                }
        );

        recyclerView = findViewById(R.id.locations_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Sorting the planning list by name. Default should be based on when it's added
        //List<LocationsListItem> loadedList = LocationsListItem.loadJSON(this, "sample_zoo_graph.json");
//        Collections.sort(loadedList, new Comparator<LocationsListItem>() {
//            @Override
//            public int compare(LocationsListItem t1, LocationsListItem t2) {
//                return t1.text.compareTo(t2.text);
//            }
//        });

        //adapter.setLocationsListItems(loadedList);

    }
    public void launchRoutePlan(View view) {
        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtra("graph_file", this.graph_file);
        intent.putExtra("vertex_file", this.node_info_file);
        intent.putExtra("edge_file", this.edge_info_file);
        startActivity(intent);
    }


    public void onClosedClicked(View view) {
        finish();
    }
}