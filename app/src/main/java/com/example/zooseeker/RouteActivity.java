package com.example.zooseeker;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.Map;

public class RouteActivity extends AppCompatActivity implements UserLocationTracker.LocationObserver
{
    private UserLocationTracker tracker;
    private DataFilesReader graphReader;
    private WarningUtilities warningUtilities;
    private Map<String, ZooData.VertexInfo> vertexInfo;

    private Route route;
    private TextView directionView;
    private boolean useBriefDirections = true;

    // Fields regarding user location, updated when tracker notifies this activity
    private Location userLocation;
    private String nearestLocationId;

    private ActivityResultLauncher<Intent> startActivityIntent;
    private boolean in_offtrack_activity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        startActivityIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        in_offtrack_activity = false;
                    }
                }
        );

        // Setup tracking and route
        route = RouteSingleton.getRoute();
        tracker = UserLocationTrackerSingleton.getTracker();
        tracker.registerObserver(this);

        // Setup directions view and warnings
        directionView = findViewById(R.id.directions);
        warningUtilities = new WarningUtilities();

        // Setup exit button
        Button exit_button = findViewById(R.id.exit_button);
        exit_button.setOnClickListener(this::onClosedClicked);

        // Setup next button
        Button nextButton = findViewById(R.id.next_route_button);
        nextButton.setOnClickListener(v -> {
            goToNextExhibit();
        });

        // Setup previous button
        Button prevButton = findViewById(R.id.prev_route_button);
        prevButton.setOnClickListener(v -> {
            goToPrevExhibit();
        });

        // Setup skip button
        Button skipButton = findViewById(R.id.skip_button);
        skipButton.setOnClickListener(v -> {
            skipExhibit();
        });

        // Setup directions style toggle
        SwitchCompat directionsToggle = findViewById(R.id.directions_style_toggle);
        directionsToggle.setOnClickListener(v -> {
            // If switch is checked, use detailed directions
            useBriefDirections = !directionsToggle.isChecked();
            updateDirectionsDisplay();
        });


        // Setup mock injection button
        Button injectLocationButton = findViewById(R.id.inject_location_button);
        injectLocationButton.setOnClickListener(v -> {
            int inputType = EditorInfo.TYPE_CLASS_NUMBER
                    | EditorInfo.TYPE_NUMBER_FLAG_SIGNED
                    | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL;

            final EditText latInput = new EditText(this);
            latInput.setInputType(inputType);
            latInput.setHint("Latitude");
            latInput.setText("32.737986");

            final EditText lngInput = new EditText(this);
            lngInput.setInputType(inputType);
            lngInput.setHint("Longitude");
            lngInput.setText("-117.169499");

            final LinearLayout layout = new LinearLayout(this);
            layout.setDividerPadding(8);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(latInput);
            layout.addView(lngInput);

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Inject a Mock Location")
                    .setView(layout)
                    .setPositiveButton("Submit", (dialog, which) -> {
                        double lat = Double.parseDouble(latInput.getText().toString());
                        double lng = Double.parseDouble(lngInput.getText().toString());
                        Location mockLocation = new Location("");
                        mockLocation.setLatitude(lat);
                        mockLocation.setLongitude(lng);
                        update(mockLocation);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.cancel();
                    });
            builder.show();
        });

        // Retrieve asset file and graph objects
        Intent intent = getIntent();
        this.graphReader = new DataFilesReader(this, intent.getExtras().getString("assets_list_file"));
        this.vertexInfo = graphReader.getVertexInfo();

        // Get locations
        this.userLocation = tracker.getUserLocation();
        this.nearestLocationId = getNearestLocationId();

        // Set directions
        updateDirectionsDisplay();
    }

    /**
     * Pulls exhibit id in zoo closest to user's lat and lng
     * @return string id of nearest location vertex in zoo
     */
    private String getNearestLocationId() {
        // Pull user location and find nearest zoo location
        Location userLocation = this.userLocation;
        return (userLocation == null)
                ? graphReader.getGateId() : Route.getNearestLocationId(vertexInfo, userLocation);
    }

    /**
     * Method handling logic to go to next exhibit in route list
     */
    private void goToNextExhibit() {
        if (!route.reachedEnd()) {
            route.advanceToNextExhibit(this.nearestLocationId);
        } else {
            warningUtilities.showError(this, "Already at end of route!");
        }
        updateDirectionsDisplay();
    }

    /**
     * Method handling logic to go to previous exhibit in route list
     */
    private void goToPrevExhibit() {
        if (!route.atStart()) {
            route.returnToPreviousExhibit(this.nearestLocationId);
        } else {
            warningUtilities.showError(this, "No exhibit to go back to!");
        }
        updateDirectionsDisplay();
    }

    /**
     * Skips the next exhibit in the route plan
     */
    private void skipExhibit() {
        if (!route.reachedEnd()) {
            route.skipNextExhibit(this.nearestLocationId);
        } else {
            warningUtilities.showError(this, "Already at end of route!");
        }
        updateDirectionsDisplay();
    }

    /**
     * Updates the textview to have next directions, with next exhibit on top
     */
    private void updateDirectionsDisplay() {
        String directionsContent = "";
        directionsContent = directionsContent + "Next Exhibit: "
                + route.routeOrder.get(route.getNextExhibitIndex()).name + "\n\n"
                + condenseDirectionsList(route.getDirectionsToNextExhibit(this.nearestLocationId, useBriefDirections));
        directionView.setText(directionsContent);
    }

    /**
     * Utility function to get one string of directions
     * @param directionsList - list of strings representing direction
     * @return String representing collated directions
     */
    public static String condenseDirectionsList(ArrayList<String> directionsList) {
        String finalString = "";
        for (String s : directionsList) {
            finalString = finalString + s + "\n\n";
        }
        return finalString;
    }

    /**
     * Updates fields and reroutes if needed, when user changes location as notified by tracker
     * @param location - New location of user
     */
    public void update(Location location) {
        // Update location fields
        this.userLocation = location;
        this.nearestLocationId = getNearestLocationId();

        // If user is in offtrack selection, do not request offtrack activity
        if (in_offtrack_activity) return;

        // Off-track logic: if user off-track, offer to reroute remaining exhibits
        if (route.isOffTrack(nearestLocationId)) {
            in_offtrack_activity = true;
            Intent offtrackIntent = new Intent(this, OffTrackActivity.class);
            offtrackIntent.putExtra("new_start_id", nearestLocationId);
            startActivityIntent.launch(offtrackIntent);
        } else {
            updateDirectionsDisplay();
        }
    }

    public void onClosedClicked(View view) {
        finish();
    }
}
