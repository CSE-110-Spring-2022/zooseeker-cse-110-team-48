package com.example.zooseeker;

import static org.robolectric.RuntimeEnvironment.getApplication;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
public class RouteActivityTest {
    public static final String ASSETS_LIST_FILE = "zoo_data_files.json";

    private final Context context = getApplication().getApplicationContext();
    private final DataFilesReader graphReader = new DataFilesReader(context, ASSETS_LIST_FILE);
    private ArrayList<String> plannedExhibitIds;

    @Before
    public void setupPlannedExhibitIds() {
        this.plannedExhibitIds = new ArrayList<>();
    }

    @Test
    public void testUpdateDirections() {
        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                UserLocationTrackerSingleton.getTracker().useGPSLocations = false;
            });
        }
        this.plannedExhibitIds.add("orangutan");
        this.plannedExhibitIds.add("fern_canyon");
        Route route = new Route(this.graphReader, this.plannedExhibitIds, "orangutan");
        RouteSingleton.setRoute(route);
        final Intent intent = new Intent(this.context, RouteActivity.class);
        intent.putExtra("assets_list_file", ASSETS_LIST_FILE);
        try (ActivityScenario<RouteActivity> scenario = ActivityScenario.launch(intent
        )) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.next_route_button).performClick();
                activity.update(getLocation("parker_aviary"));
                TextView directionsView = activity.findViewById(R.id.directions);
                String fromParker = (String) directionsView.getText();
                activity.update(getLocation("owens_aviary"));
                String fromOwens = (String) directionsView.getText();
                Assert.assertFalse(fromParker.equals(fromOwens));
            });
        }
    }

    @NonNull
    private Location getLocation(String exhibitId) {
        ZooData.VertexInfo exhibitInfo = this.graphReader.getVertexInfo().get(exhibitId);
        Location exhibitLocation = new Location("");
        exhibitLocation.setLatitude(Objects.requireNonNull(exhibitInfo).lat);
        exhibitLocation.setLongitude(exhibitInfo.lng);
        return exhibitLocation;
    }
}
