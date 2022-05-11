package com.example.zooseeker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.RuntimeEnvironment.getApplication;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jgrapht.Graph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    public Graph<String, IdentifiedWeightedEdge> zooGraph;
    public Map<String, ZooData.VertexInfo> vertexInfo;
    public Map<String, ZooData.EdgeInfo> edgeInfo;
    public ArrayList<String> targets;

    // Loads the graphs
    @Before
    public void setup() {
        this.zooGraph = ZooData.loadZooGraphJSON(context, GRAPH_FILE);
        this.vertexInfo = ZooData.loadVertexInfoJSON(context, NODE_INFO);
        this.edgeInfo = ZooData.loadEdgeInfoJSON(context, EDGE_INFO);
        this.targets = new ArrayList<>();
    }

    @Test
    public void testTriGraph() {
        Graph<String, IdentifiedWeightedEdge> zooGraph = ZooData.loadZooGraphJSON(context, TRI_GRAPH);
        this.targets.add("lions");
        this.targets.add("gators");

        GraphRoute route = new GraphRoute(zooGraph, this.vertexInfo, this.edgeInfo, this
                .targets, "entrance_exit_gate");
        assertEquals(3, route.getPathEdges().getLength());

        ArrayList<String> expectedOrder = new ArrayList<>();
        expectedOrder.add("entrance_exit_gate");
        expectedOrder.add("gators");
        expectedOrder.add("lions");
        expectedOrder.add("entrance_exit_gate");
        Assert.assertEquals(expectedOrder, route.exhibitsInOrder());

        Assert.assertEquals(200.0, route.getRouteDistance("gators"), 0.01);
        Assert.assertEquals(390.0, route.getRouteDistance("lions"), 0.01);
    }

    @Test
    public void testGivenGraph() {
        this.targets.add("gorillas");
        this.targets.add("gators");

        GraphRoute route = new GraphRoute(this.zooGraph, this.vertexInfo, this.edgeInfo, this
                .targets, "entrance_exit_gate");
        assertEquals(6, route.getPathEdges().getLength());

        ArrayList<String> expectedOrder = new ArrayList<>();
        expectedOrder.add("entrance_exit_gate");
        expectedOrder.add("gators");
        expectedOrder.add("gorillas");
        expectedOrder.add("entrance_exit_gate");
        Assert.assertEquals(expectedOrder, route.exhibitsInOrder());
    }

    @Test
    public void testRouteDirections() {
        targets.add("lions");
        targets.add("gorillas");

        ArrayList<String> directions = new ArrayList<>();
        GraphRoute route = new GraphRoute(this.zooGraph, this.vertexInfo, this.edgeInfo, this
                .targets, "entrance_exit_gate");
        while (!route.reachedEnd()) {
            directions.addAll(route.advanceToNextExhibit());
        }
        Assert.assertEquals(6, directions.size());
    }

    @Test
    public void testAdvanceToNextExhibit() {
        targets.add("elephant_odyssey");
        targets.add("arctic_foxes");
        targets.add("lions");
        GraphRoute route = new GraphRoute(this.zooGraph, this.vertexInfo, this.edgeInfo, this
                .targets, "entrance_exit_gate");
        route.advanceToNextExhibit();
        assertEquals("Arctic Avenue", route.currStreet);
        assertEquals(1, route.nextExhibitIndex);
        assertEquals(2, route.currentEdgeIndex);
        route.advanceToNextExhibit();
        assertEquals("Sharp Teeth Shortcut", route.currStreet);
        assertEquals(2, route.nextExhibitIndex);
        assertEquals(5, route.currentEdgeIndex);
    }

    @Test
    public void testAdvanceToNextExhibit2() {
        targets.add("arctic_foxes");
        GraphRoute route = new GraphRoute(this.zooGraph, this.vertexInfo, this.edgeInfo, this
                .targets, "entrance_exit_gate");
        route.advanceToNextExhibit();
        assertEquals("Arctic Avenue", route.currStreet);
        assertEquals(1, route.nextExhibitIndex);
        assertEquals(2, route.currentEdgeIndex);
    }

    @Test
    public void testReachedEnd() {
        targets.add("arctic_foxes");
        GraphRoute route = new GraphRoute(this.zooGraph, this.vertexInfo, this.edgeInfo, this
                .targets, "entrance_exit_gate");
        assertFalse(route.reachedEnd());
        route.advanceToNextExhibit();
        route.advanceToNextExhibit();
        assertTrue(route.reachedEnd());
    }
}
