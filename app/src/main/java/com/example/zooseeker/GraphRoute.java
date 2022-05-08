package com.example.zooseeker;

import android.content.Context;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**\
 * This class represents an exhibit route, plotted using a TSP heuristic
 */
public class GraphRoute {
    // Fields for graph properties
    Graph<String, IdentifiedWeightedEdge> zooGraph;
    Map<String, ZooData.VertexInfo> vertexInfo;
    Map<String, ZooData.EdgeInfo> edgeInfo;

    String startVertex;
    String currStreet; // Street the user is at

    GraphPath<String, IdentifiedWeightedEdge> pathEdges;
    ArrayList<String> exhibitOrder = new ArrayList<String>(); // Order of exhibits of interest
    List<IdentifiedWeightedEdge> edgeList;
    Map<String, Double> exhibitDistances = new HashMap<>();

    int nextExhibitIndex = 0; // Indexes the progress of user in the route, in terms of exhibit
                                // indices in exhibitOrder
    int currentEdgeIndex = 0; // Indexes the edge the user is at, whilst traversing the route

    /**
     * Route constructor, using a TSP shortest-path heuristic.
     *  Returns a cycle starting and ending at startVertex, passing through targets.
     * @param zooGraph - graph to operate on
     * @param startVertex - starting and ending point
     * @param targets - list of exhibits to visit, EXCLUDING startVertex
     */
    public GraphRoute(
            Graph<String, IdentifiedWeightedEdge> zooGraph,
            Map<String, ZooData.VertexInfo> vertexInfo,
            Map<String, ZooData.EdgeInfo> edgeInfo,
            ArrayList<String> targets,
            String startVertex) {

        // Set route's graph fields and start vertex
        this.zooGraph = zooGraph;
        this.vertexInfo = vertexInfo;
        this.edgeInfo = edgeInfo;
        this.startVertex = startVertex;

        // Unvisited targets array keeps track of which vertices to find a path to
        ArrayList<String> unvisitedTargets = new ArrayList<String>(targets);

        // Create DSP object to compute path costs
        DijkstraShortestPath<String, IdentifiedWeightedEdge> dijkstraShortestPath
                = new DijkstraShortestPath<String, IdentifiedWeightedEdge>(zooGraph);

        // Add the starting point to exhibit order, and create objects to track route creation
        String currVertex = startVertex;
        exhibitOrder.add(startVertex);
        GraphPath<String, IdentifiedWeightedEdge> currPath;
        ArrayList<IdentifiedWeightedEdge> totalEdges = new ArrayList<IdentifiedWeightedEdge>();
        double totalWeight = .0;

        // Loop through all exhibits of interest until we visit all of them
        while (unvisitedTargets.size() > 0) {
            // Temp variables until closest exhibit is found
            String closestVertex = unvisitedTargets.get(0);
            double closestPathWeight = Double.MAX_VALUE;
            double tempPathWeight;

            // Find the closest vertex
            for (String vertex : unvisitedTargets) {
                tempPathWeight = dijkstraShortestPath.getPathWeight(currVertex, vertex);
                if (tempPathWeight < closestPathWeight) {
                    closestVertex = vertex;
                    closestPathWeight = tempPathWeight;
                }
            }

            // Go to next closest exhibit
            currPath = dijkstraShortestPath.getPath(currVertex, closestVertex);
            // Add path to total path
            for (IdentifiedWeightedEdge edge : currPath.getEdgeList()) {
                totalEdges.add(edge);
            }
            totalWeight += currPath.getWeight();

            // Update current exhibit, and add exhibit to route fields
            currVertex = closestVertex;
            exhibitOrder.add(closestVertex);
            exhibitDistances.put(closestVertex, totalWeight);
            unvisitedTargets.remove(closestVertex);
        }

        // Return to startVertex
        currPath = dijkstraShortestPath.getPath(currVertex, startVertex);
        for (IdentifiedWeightedEdge edge : currPath.getEdgeList()) {
            totalEdges.add(edge);
        }
        exhibitOrder.add(startVertex);
        totalWeight += currPath.getWeight();

        this.pathEdges = new GraphWalk<String, IdentifiedWeightedEdge>(
                zooGraph, startVertex, startVertex, totalEdges, totalWeight);
        this.edgeList = getEdgeList();
    }

    /**
     * Getter for edge list of route cycle
     * @return list of edges from route cycle
     */
    public List<IdentifiedWeightedEdge> getEdgeList() {
        return this.pathEdges.getEdgeList();
    }

    /**
     * Getter for the route walk
     * @return GraphPath containing route to traverse
     */
    public GraphPath<String, IdentifiedWeightedEdge> getPathEdges() {
        return pathEdges;
    }

    /**
     * Returns the list of exhibits to visit, in order in the cycle.
     *  Includes Start and End vertices
     * @return
     */
    public ArrayList<String> exhibitsInOrder() {
        return this.exhibitOrder;
    }

    /**
     * Find route distance for an exhibit
     * @param exhibit - name of exhibit to find route
     * @return distance to get to exhibit. -1 if invalid exhibit.
     */
    public double getRouteDistance(String exhibit) {
        try {
            return exhibitDistances.get(exhibit);
        } catch (NullPointerException e) {
            return -1;
        }
    }

    /**
     * When called, advances the GraphRoute object to the next exhibit of interest in ExhibitOrder,
     *  returning a formatted list of directions
     * @return directions - ArrayList containing human-readable directions to get to the next exhibit
     */
    public ArrayList<String> advanceToNextExhibit() {
        // Strings tracking position in graph as we advance to the next POI
        String currentLocation;
        String nextLocation;
        // Returned directions list, formatted for display
        ArrayList<String> directions = new ArrayList<>();

        // Get the current vertex (currentLocation), and next POI (destExhibit)
        currentLocation = exhibitOrder.get(nextExhibitIndex);
        this.nextExhibitIndex++;
        String destExhibit = exhibitOrder.get(nextExhibitIndex);

        boolean reachedNextLocation = false;
        // Traverse edges, until after we encounter an edge with the destExhibit vertex
        do {
            IdentifiedWeightedEdge currentEdge = edgeList.get(currentEdgeIndex);

            // Check if we'll be done after the loop
            if (edgeHasExhibit(currentEdge, destExhibit)) {
                reachedNextLocation = true;
            }

            // Whichever vertex is not the current location, is the next vertex to visit
            String source = zooGraph.getEdgeSource(currentEdge);
            String target = zooGraph.getEdgeTarget(currentEdge);
            if (source.equals(currentLocation)) {
                nextLocation = target;
            } else {
                nextLocation = source;
            }
            // Format the edge to be readable in direction text
            directions.add(edgeToDirection(currentEdge, nextLocation, currStreet));
            currStreet = edgeInfo.get(currentEdge.getId()).street;

            // We advance the edge index tracking our progress in the path, and advance vertices
            currentEdgeIndex++;
            currentLocation = nextLocation;
        } while (!reachedNextLocation);

        return directions;
    }

    /**
     * Checks if an edge's vertices contains an exhibit. Required because the graph is undirected,
     *  and "source" / "target" are arbitrary
     * @param e - edge to check vertices for
     * @param exhibit - exhibit to check against
     * @return true if the edge's endpoints contains exhibit as a vertex
     */
    public boolean edgeHasExhibit(IdentifiedWeightedEdge e, String exhibit) {
        return (zooGraph.getEdgeTarget(e).equals(exhibit) || zooGraph.getEdgeSource(e).equals(exhibit));
    }

    /**
     * Returns the human-readable directions associated with an edge
     * @param e - edge to convert to text directions
     * @param nextLocation - destination vertex of edge e
     * @param prevStreet - street of previous direction. Needed to determine continue or proceed
     * @return result - string direction for output
     */
    public String edgeToDirection(IdentifiedWeightedEdge e, String nextLocation, String prevStreet) {
        // Get street name, distance to walk, and destination vertex name
        String street = edgeInfo.get(e.getId()).street;
        double distance = zooGraph.getEdgeWeight(e);
        String destination = (vertexInfo.get(nextLocation).name);

        // Initialize result. If the directions is still on the same street, say "Continue..."
        String result;
        if ((prevStreet != null) && (prevStreet.equals(street))) {
            result = "Continue on ";
        }
        else {
            result = "Proceed on ";
        }
        // Add street name, distance, and destination vertex to direction result
        result = result + street + " " + (int) distance + " ft towards " + destination;
        if (vertexInfo.get(nextLocation).kind.toString() == "EXHIBIT") {
            result = result + " Exhibit";
        }
        return result;
    }

    /**
     * Checks if the route is completed
     * @return true if route is completed, i.e. we have returned at the starting vertex
     */
    public boolean reachedEnd() {
        return nextExhibitIndex + 1 >= exhibitOrder.size();
    }
}
