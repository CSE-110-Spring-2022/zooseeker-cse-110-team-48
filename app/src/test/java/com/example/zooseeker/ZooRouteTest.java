package com.example.zooseeker;

import static org.robolectric.RuntimeEnvironment.getApplication;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ZooRouteTest {
    // Paths to files
    public final String GRAPH_FILE = "sample_zoo_graph.json";
    public final String NODE_INFO = "sample_node_info.json";
    public final String EDGE_INFO = "sample_edge_info.json";
    public final String TRI_GRAPH = "triangular_graph.json";

    public Context context = getApplication().getApplicationContext();

    @Test
    public void testTriGraph() {
        // Load graph into app
        Graph<String, IdentifiedWeightedEdge> zooGraph = ZooData.loadZooGraphJSON(context, TRI_GRAPH);

        ArrayList<String> targets = new ArrayList<String>();
        targets.add("lions");
        targets.add("gators");

        GraphPath<String, IdentifiedWeightedEdge> route = GraphRouteFinder.findRoute(zooGraph, targets, "entrance_exit_gate");
        assertEquals(3, route.getLength());
    }

    @Test
    public void testGivenGraph() {
        // Load graph into app
        Graph<String, IdentifiedWeightedEdge> zooGraph = ZooData.loadZooGraphJSON(context, GRAPH_FILE);

        ArrayList<String> targets = new ArrayList<String>();
        targets.add("gorillas");
        targets.add("gators");

        GraphPath<String, IdentifiedWeightedEdge> route = GraphRouteFinder.findRoute(zooGraph, targets, "entrance_exit_gate");
        assertEquals(6, route.getLength());
    }

}
