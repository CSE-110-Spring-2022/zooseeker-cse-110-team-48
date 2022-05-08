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

import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LocationsListActivity extends AppCompatActivity {
    public RecyclerView recyclerView;
    private LocationsListViewModel viewModel;

    // Paths to files
    public String graph_file;
    public String node_info_file;
    public String edge_info_file;

    List<String> exhibitsInPlan;
    GraphRoute route;

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

        Graph<String, IdentifiedWeightedEdge> zooGraph = ZooData.loadZooGraphJSON(this, graph_file);
        Map<String, ZooData.VertexInfo> vertexInfo = ZooData.loadVertexInfoJSON(this, node_info_file);
        Map<String, ZooData.EdgeInfo> edgeInfo = ZooData.loadEdgeInfoJSON(this, edge_info_file);

        LocationsDatabase db = LocationsDatabase.getSingleton(this);
        LocationsListItemDao locationsListItemDao = db.locationsListItemDao();

        List<LocationsListItem> exhibitsToVisit = locationsListItemDao.getAll();
        ArrayList<String> targets = new ArrayList<String>();
        for (LocationsListItem element : exhibitsToVisit) {
            targets.add(element.textId);
        }

        this.route = new GraphRoute(zooGraph, vertexInfo, edgeInfo, targets, "entrance_exit_gate");
        this.exhibitsInPlan = route.exhibitsInOrder();

        List<LocationsListItem> unorderedExhibits = locationsListItemDao.getAll();
        locationsListItemDao.deleteAll();

        for(int i = 1; i < exhibitsInPlan.size() - 1; i++ ) {
            String location = exhibitsInPlan.get(i);
            String properName = "";
            for (LocationsListItem item : unorderedExhibits) {
                if (item.textId.equals(location)) {
                    properName = item.text;
                }
            }
            double distance = route.getRouteDistance(location);
            viewModel.createLocation(properName, location, distance);
        }

    }
    public void launchRoutePlan(View view) {
        Intent intent = new Intent(this, RouteActivity.class);
        ArrayList<String> directions = new ArrayList<>();
        while (!this.route.reachedEnd()) {
            directions.add(GraphRoute.condenseDirectionsList(route.advanceToNextExhibit()));
        }
        intent.putExtra("directions_list", directions);
        startActivity(intent);
    }


    public void onClosedClicked(View view) {
        finish();
    }
}