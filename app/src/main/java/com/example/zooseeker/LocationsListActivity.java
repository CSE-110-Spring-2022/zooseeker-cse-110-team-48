package com.example.zooseeker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractBaseGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for the list of locations to visit, called from MainActivity
 */
public class LocationsListActivity extends AppCompatActivity {
    public RecyclerView recyclerView;
    private LocationsListViewModel viewModel;
    private LocationsListItemDao locationsListItemDao;
    private String asset_file;
    private DataFilesReader graphReader;
    private UserLocationTracker tracker;

    // Map containing zoo location info
    private Map<String, ZooData.VertexInfo> vertexInfo;
    private Route route;
    private String nearestLocationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_list);
        Intent intent = getIntent();

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

        // Create user location tracker
        this.tracker = UserLocationTrackerSingleton.getTracker();

        // Get zoo graph objects
        this.asset_file = intent.getExtras().getString("assets_list_file");
        this.graphReader = new DataFilesReader(this, asset_file);
        this.vertexInfo = graphReader.getVertexInfo();

        // Pull user location and find nearest zoo location
        Location userLocation = tracker.getUserLocation();
        this.nearestLocationId = (userLocation == null)
                ? graphReader.getGateId() : Route.getNearestLocationId(vertexInfo, userLocation);

        createRoute();
    }

    /**
     * Pulls exhibit id in zoo closest to user's lat and lng
     * @return string id of nearest location vertex in zoo
     */
    private String getNearestLocationId() {
        // Pull user location and find nearest zoo location
        Location userLocation = tracker.getUserLocation();
        return (userLocation == null)
                ? graphReader.getGateId() : Route.getNearestLocationId(vertexInfo, userLocation);
    }

    /**
     * This methods creates a route based on database exhibit entries
     */
    private void createRoute() {
        // Get database instance
        LocationsDatabase db = LocationsDatabase.getSingleton(this);
        locationsListItemDao = db.locationsListItemDao();

        // Get exhibits to visit
        List<LocationsListItem> exhibitsToVisit = locationsListItemDao.getAll();
        ArrayList<String> exhibitIds = new ArrayList<>();
        for (LocationsListItem element : exhibitsToVisit) {
            exhibitIds.add(element.textId);
        }

        // Construct route (TSP heuristic), and retrieve major vertices in route planned
        this.route = new Route(this.graphReader, exhibitIds, getNearestLocationId());

        // Retrieve all exhibits but last exit gate
        List<String> exhibitIdsToVisit = route.getExhibitsInOrder().subList(0, route.getExhibitsInOrder().size() - 1);

        // We want to delete all locations and re-add them in proper order to the database
        locationsListItemDao.deleteAll();

        // Re-adds locations in order visited in plan, with proper distances calculated by route.
        for (String exhibitId : exhibitIdsToVisit) {
            String exhibitName = vertexInfo.get(exhibitId).name;
            double exhibitDistance = route.getTotalDistance(exhibitId, nearestLocationId);
            viewModel.createLocation(exhibitName, exhibitId, exhibitDistance);
        }

        // Set singleton route object to this route
        RouteSingleton.setRoute(route);
    }

    /**
     * Launches directions activity, passing serialized array of directions through intent.putExtra
     */
    public void launchRoutePlan(View view) {
        if (locationsListItemDao.getDataCount() == 0){
            Utilities.showAlert(this, "Add at least one exhibit to your route!");
        }else {
            createRoute();
            Intent intent = new Intent(this, RouteActivity.class);
            intent.putExtra("assets_list_file", asset_file); // Pass assets file paths
            startActivity(intent);
        }
    }

    public void onClosedClicked(View view) {
        finish();
    }
}