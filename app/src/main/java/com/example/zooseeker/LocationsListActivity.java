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

/**
 * Activity for the list of locations to visit, called from MainActivity
 */
public class LocationsListActivity extends AppCompatActivity {
    public static final String GRAPH_ROUTE_KEY = "route";
    public RecyclerView recyclerView;
    private LocationsListViewModel viewModel;
    private ArrayList<String> directions;
    LocationsListItemDao locationsListItemDao;

    // Objects containing info of graphs
    private Graph<String, IdentifiedWeightedEdge> zooGraph;
    private Map<String, ZooData.VertexInfo> vertexInfo;
    private Map<String, ZooData.EdgeInfo> edgeInfo;

    List<String> exhibitsInPlan;
    GraphRoute route;
    String startVertex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_list);
        Intent intent = getIntent();

        // Get zoo graph objects
        DataFilesReader graphReader = new DataFilesReader(this, intent.getExtras().getString("assets_list_file"));
        this.zooGraph = graphReader.getGraph();
        this.vertexInfo = graphReader.getVertexInfo();
        this.edgeInfo = graphReader.getEdgeInfo();
        this.startVertex = graphReader.getGateId();

        // Initialize view model for plan list
        LocationsListViewModel viewModel = new ViewModelProvider(this).get(LocationsListViewModel.class);
        this.viewModel = viewModel;

        // Set up adapter connection between recyclerView and database of locations
        LocationsListAdapter adapter = new LocationsListAdapter();
        adapter.setHasStableIds(true);
        adapter.setOnDeleteClickedHandler(viewModel::deleteLocation);
        viewModel.getLocationsListItems().observe(this, adapter::setLocationsListItems);

        // Hook up "Directions" button in UI to show directions
        Button planRouteButton = findViewById(R.id.plan_route_btn);
        planRouteButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        launchRoutePlan(v);
                    }
                }
        );

        // Finish creating adapter connection between recyclerView and database of locations
        recyclerView = findViewById(R.id.locations_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        updateDirections(startVertex);
    }

    /**
     * This method creates a GraphRoute using the locations in the database. The GraphRoute object
     *  handles directions generation, creating an ArrayList of directions to pass to the directions
     *  Activity.
     * @param startVertex - ID of starting vertex of route
     */
    private void updateDirections(String startVertex) {
        // Get database instance
        LocationsDatabase db = LocationsDatabase.getSingleton(this);
        locationsListItemDao = db.locationsListItemDao();

        // Get exhibits to visit
        List<LocationsListItem> exhibitsToVisit = locationsListItemDao.getAll();
        ArrayList<String> targets = new ArrayList<>();
        for (LocationsListItem element : exhibitsToVisit) {
            targets.add(element.textId);
        }

        // Construct route (TSP heuristic), and retrieve major vertices in route planned
        this.route = new GraphRoute(zooGraph, vertexInfo, edgeInfo, targets, startVertex);
        this.exhibitsInPlan = route.exhibitsInOrder();

        // We want to delete all locations and re-add them in proper order to the database
        List<LocationsListItem> unorderedExhibits = locationsListItemDao.getAll();
        locationsListItemDao.deleteAll();

        // Re-adds locations in order visited in plan, with proper distances calculated by route.
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

        // Keeps getting directions from GraphRoute, condensed into a ArrayList<String> of directions
        this.directions = new ArrayList<>();

        // Generate directions if there are exhibits to generate directions to.
        if (locationsListItemDao.getDataCount() > 0) {
            while (!this.route.reachedEnd()) {
                this.directions.add(GraphRoute.condenseDirectionsList(route.advanceToNextExhibit()));
            }
        }
    }

    /**
     * Launches directions activity, passing serialized array of directions through intent.putExtra
     */
    public void launchRoutePlan(View view) {
        if(locationsListItemDao.getDataCount() == 0){
            Utilities.showAlert(this, "Add at least one exhibit to your route!");
        }else {
            updateDirections(startVertex);
            Intent intent = new Intent(this, RouteActivity.class);
            intent.putExtra("directions_list", this.directions);
            startActivity(intent);
        }
    }

    public void onClosedClicked(View view) {
        finish();
    }
}