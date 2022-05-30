package com.example.zooseeker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ArrayAdapter;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnAdapterItemClickListener {

    // Path to json file containing assets to load
    public final String ASSETS_LIST_FILE = "zoo_data_files.json";

    // Databasing objects
    private LocationsDatabase db;
    private LocationsListItemDao locationsListItemDao;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get graph vertices
        DataFilesReader graphReader = new DataFilesReader(this, ASSETS_LIST_FILE);
        Map<String, ZooData.VertexInfo> vertexSet = graphReader.getVertexInfo();
        Map<String, ZooData.VertexInfo> vertexInfo = vertexSet;

        // Get exhibits list for searching
        List<Exhibit> exhibitList = Exhibit.returnExhibits(vertexInfo);

        // Instantiate AutoCompleteTextView using custom exhibit adapter
        ArrayAdapter<Exhibit> exhibitSearchAdapter = new ExhibitSearchAdapter(
                this, android.R.layout.select_dialog_item, exhibitList);
        AutoCompleteTextView searchView = (AutoCompleteTextView) findViewById(R.id.search_field);
        searchView.setThreshold(1);
        searchView.setAdapter(exhibitSearchAdapter);

        // Instantiate database objects
        Context context = getApplication().getApplicationContext();
        this.db = LocationsDatabase.getSingleton(context);
        this.locationsListItemDao = db.locationsListItemDao();

        // Instantiate and set global singleton tracker
        UserLocationTracker tracker = new UserLocationTracker(this);
        UserLocationTrackerSingleton.setTracker(tracker);

        // Setup debug toggle for using real GPS
        Button debug_gps_btn = findViewById(R.id.debug_gps_toggle_button);
        debug_gps_btn.setOnClickListener(v -> {
            // Toggle tracker's use of real locations
            boolean willUseGPS = !tracker.useGPSLocations;
            if (willUseGPS) {
                debug_gps_btn.setText("GPS Enabled");
            } else {
                debug_gps_btn.setText("GPS Disabled");
            }
            tracker.useGPSLocations = willUseGPS;
        });

        // Instantiate button to view planning list
        Button planningListButton = findViewById(R.id.view_list_btn);
        updateListCount();

        // Triggered upon selecting suggested item from search list
        searchView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Extract necessary exhibit info to construct database item
                        String queryId = exhibitSearchAdapter.getItem(position).id;
                        String queryName = exhibitSearchAdapter.getItem(position).name;

                        // Add database entry for added exhibit, if not already in database
                        if (locationsListItemDao.get(queryId) == null) {
                            int endOfListOrder = locationsListItemDao.getOrderForAppend();
                            LocationsListItem newItem = new LocationsListItem(queryName, queryId, 0, endOfListOrder);
                            locationsListItemDao.insert(newItem);
                        }

                        // Update UI elements
                        updateListCount();
                        searchView.setText("");
                    }
                }
        );

        // Initialize view model for selected exhibits list (unordered)
        LocationsListViewModel viewModel = new ViewModelProvider(this).get(LocationsListViewModel.class);

        // Set up adapter connection between recyclerView and database of locations
        LocationsListAdapter adapter = new LocationsListAdapter(this);
        adapter.setHasStableIds(true);
        adapter.setOnDeleteClickedHandler(
                viewModel::deleteLocation
        );

        viewModel.getLocationsListItems().observe(this, adapter::setLocationsListItems);
        // Finish creating adapter connection between recyclerView and database of locations
        RecyclerView recyclerView = findViewById(R.id.unordered_exhibits_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Launches the planning list
        planningListButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        launchPlanningList(v);
                    }
                }
        );

    }

    /**
     * Updates the list size count when item is deleted in the RecyclerView
     */
    @Override
    public void onAdapterItemClickListener() {
        updateListCount();
    }

    /**
     * Launches activity for viewing / editing planning list
     * @param view - current view
     */
    public void launchPlanningList(View view) {
        Intent intent = new Intent(this, LocationsListActivity.class);
        intent.putExtra("assets_list_file", ASSETS_LIST_FILE); // Pass assets file paths
        startActivity(intent);
    }

    public void onResume() {
        super.onResume();
        updateListCount();
    }

    /**
     * Updates the list displayed on the planning list button
     */
    public void updateListCount(){
        Context context = getApplication().getApplicationContext();
        db = LocationsDatabase.getSingleton(context);
        locationsListItemDao = db.locationsListItemDao();

        Button planningListButton = findViewById(R.id.view_list_btn);
        planningListButton.setText("Plan (" + locationsListItemDao.getDataCount() + ")");
    }
}