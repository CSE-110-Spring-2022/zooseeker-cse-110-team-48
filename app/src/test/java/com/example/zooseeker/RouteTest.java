package com.example.zooseeker;

import static org.robolectric.RuntimeEnvironment.getApplication;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class RouteTest {

    // Create objects needed for Route to function
    public Context context = getApplication().getApplicationContext();
    public DataFilesReader graphReader = new DataFilesReader(context, "zoo_data_files.json");

    // Covers Ctor, closestExhibitId, getWaypoint
    @Test
    public void testRouteCreation() {
        ArrayList<String> exhibits = new ArrayList<>();
        exhibits.add("toucan");
        exhibits.add("siamang");
        exhibits.add("crocodile");
        Route route = new Route(graphReader, exhibits, "entrance_exit_gate");

        // Check ordering of exhibits
        Assert.assertTrue(route.routeOrder.get(0).id.equals("siamang"));
        Assert.assertTrue(route.routeOrder.get(1).id.equals("toucan"));
        Assert.assertTrue(route.routeOrder.get(2).id.equals("crocodile"));
        Assert.assertTrue(route.routeOrder.get(3).id.equals("entrance_exit_gate"));
    }

    // Covers getPath and getPathWeight
    @Test
    public void testGetPathAndWeight() {
        ArrayList<String> exhibits = new ArrayList<>();
        exhibits.add("motmot");
        exhibits.add("fern_canyon");
        Route route = new Route(graphReader, exhibits, "entrance_exit_gate");

        List<IdentifiedWeightedEdge> edges = route.getPath("gorilla", "fern_canyon").getEdgeList();
        Assert.assertEquals(edges.size(), 4);

        Assert.assertEquals(route.getPathWeight("motmot", "fern_canyon"), 3000, 1);
        Assert.assertEquals(route.getPathWeight("fern_canyon", "motmot"), 3000, 1);
    }

    // Covers getTotalDistance
    @Test
    public void testGetTotalDistance() {
        ArrayList<String> exhibits = new ArrayList<>();
        exhibits.add("toucan");
        exhibits.add("siamang");
        exhibits.add("crocodile");
        Route route = new Route(graphReader, exhibits, "entrance_exit_gate");

        Assert.assertEquals(route.getTotalDistance("siamang", "entrance_exit_gate"), 4800, 1);
        Assert.assertEquals(route.getTotalDistance( "toucan", "entrance_exit_gate"), 7400, 1);
        Assert.assertEquals(route.getTotalDistance("crocodile", "entrance_exit_gate"), 12700, 1);
    }

    // Covers advanceToNextExhibit, getDirections, edgeToDirection
    @Test
    public void testNextRouteProgression() {
        ArrayList<String> exhibits = new ArrayList<>();
        exhibits.add("siamang");
        exhibits.add("toucan");
        exhibits.add("crocodile");
        Route route = new Route(graphReader, exhibits, "entrance_exit_gate");

        ArrayList<String> directionsList = route.advanceToNextExhibit("entrance_exit_gate");
        assertHasStreet("Gate Path", directionsList.get(0));
        int lastIndex = directionsList.size() - 1;
        assertHasStreet("Orangutan Trail", directionsList.get(lastIndex));
        directionsList = route.advanceToNextExhibit("siamang");
        directionsList = route.advanceToNextExhibit("toucan");
        directionsList = route.advanceToNextExhibit("crocodile");
        assertHasStreet("Hippo Trail", directionsList.get(0));
        lastIndex = directionsList.size() - 1;
        assertHasStreet("Gate Path", directionsList.get(lastIndex));
        Assert.assertTrue(route.reachedEnd());
    }

    // Covers returnToPreviousExhibit
    @Test
    public void testPrevRetracking() {
        ArrayList<String> exhibits = new ArrayList<>();
        exhibits.add("flamingo");
        exhibits.add("spoonbill");
        Route route = new Route(graphReader, exhibits, "intxn_front_monkey");

        ArrayList<String> directionsList = route.advanceToNextExhibit("intxn_front_monkey");
        directionsList = route.advanceToNextExhibit("flamingo");
        directionsList = route.returnToPreviousExhibit("gorilla");
        assertNextExhibitIdEquals(route, "spoonbill");
        directionsList = route.returnToPreviousExhibit("crocodile");
        assertNextExhibitIdEquals(route, "flamingo");
        Assert.assertTrue(!route.reachedEnd());
    }

    // Covers rerouting and offtrack detection
    @Test
    public void testReroute() {
        ArrayList<String> exhibits = new ArrayList<>();
        exhibits.add("siamang");
        exhibits.add("toucan");
        exhibits.add("crocodile");
        Route route = new Route(graphReader, exhibits, "entrance_exit_gate");

        ArrayList<String> directionsList = route.advanceToNextExhibit("entrance_exit_gate");
        // Siamangs should be visited already, reroute from hippos
        if (route.isOffTrack("hippo") != true) {
            throw new AssertionError("Did not detect off-track");
        }
        route.reroute("hippo");
        assertNextExhibitIdEquals(route, "crocodile");
        directionsList = route.advanceToNextExhibit("hippo"); // Should go to crocodiles
        assertNextExhibitIdEquals(route, "toucan");
        directionsList = route.advanceToNextExhibit("crocodile"); // Should go to toucans
    }

    // Covers skipping method
    @Test
    public void testSkip() {
        ArrayList<String> exhibits = new ArrayList<>();
        exhibits.add("siamang");
        exhibits.add("toucan");
        exhibits.add("crocodile");
        Route route = new Route(graphReader, exhibits, "entrance_exit_gate");

        ArrayList<String> directionsList = route.advanceToNextExhibit("entrance_exit_gate");
        // At Siamangs, skip toucan
        route.skipNextExhibit("siamang");
        // Check that next exhibit is crocodile
        Assert.assertEquals(route.routeOrder.get(route.getNextExhibitIndex()).id, "crocodile");
        route.advanceToNextExhibit("siamang");
        route.advanceToNextExhibit("crocodile");
        Assert.assertTrue(route.reachedEnd());
    }

    // Source: https://stackoverflow.com/a/2275035
    private boolean isSubstringOf(String substring, String superstring) {
        return superstring.toUpperCase().contains(substring.toUpperCase());
    }

    private void assertHasStreet(String expected, String direction) {
        Assert.assertTrue(isSubstringOf(expected, direction));
    }

    private void assertNextExhibitIdEquals(Route route, String expected) {
        String nextExhibitId = route.routeOrder.get(route.getNextExhibitIndex()).id;
        Assert.assertEquals(expected, nextExhibitId);
    }
}
