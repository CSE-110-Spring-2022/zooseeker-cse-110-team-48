package com.example.zooseeker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteActivity extends AppCompatActivity {

    List<String> exhibitsInPlan;
    int nextExhibitIndex;
    public RecyclerView recyclerView;
    public LocationsListViewModel viewModel;
    private EditText newLocationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Button exit_button = findViewById(R.id.exit_button);



        exit_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        onClosedClicked(v);
                    }
                }
        );

        // Load graph into app
        String graphFile = getIntent().getExtras().getString("graph_file");
        String node_info_file = getIntent().getExtras().getString("vertex_file");
        String edge_info_file = getIntent().getExtras().getString("edge_file");

        Graph<String, IdentifiedWeightedEdge> zooGraph = ZooData.loadZooGraphJSON(this, graphFile);
        Map<String, ZooData.VertexInfo> vertexInfo = ZooData.loadVertexInfoJSON(this, node_info_file);
        Map<String, ZooData.EdgeInfo> edgeInfo = ZooData.loadEdgeInfoJSON(this, edge_info_file);

        // Load activities in list
        LocationsListViewModel viewModel = new ViewModelProvider(this).get(LocationsListViewModel.class);
        this.viewModel = viewModel;
        LocationsListAdapter adapter = new LocationsListAdapter();
        adapter.setHasStableIds(true);
        viewModel.getLocationsListItems().observe(this, adapter::setLocationsListItems);

        LocationsDatabase db = LocationsDatabase.getSingleton(this);
        LocationsListItemDao locationsListItemDao = db.locationsListItemDao();

        List<LocationsListItem> exhibitsToVisit = locationsListItemDao.getAll();
        ArrayList<String> targets = new ArrayList<String>();
        for (LocationsListItem element : exhibitsToVisit) {
            targets.add(element.textId);
        }

        GraphRoute route = new GraphRoute(zooGraph, vertexInfo, edgeInfo, targets, "entrance_exit_gate");
        this.exhibitsInPlan = route.exhibitsInOrder();

        recyclerView = findViewById(R.id.route_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        for(LocationsListItem location : exhibitsToVisit){
            viewModel.createLocation(location.text,location.textId);
        }





        nextExhibitIndex = 0;

        Button nextButton = findViewById(R.id.next_route_button);
        nextButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        nextLocation(route);
                    }
                }
        );
    }

    public void nextLocation(GraphRoute route) {
        ArrayList<String> directionEdges = route.advanceToNextExhibit();
    }

    public void onClosedClicked(View view) {
        finish();
    }
}