package com.example.zooseeker;

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

    GraphPath<String, IdentifiedWeightedEdge> pathEdges;
    ArrayList<String> exhibitOrder = new ArrayList<String>();
    Graph<String, IdentifiedWeightedEdge> zooGraph;
    Map<String, Double> exhibitDistances = new HashMap<>();

    /**
     * Route constructor, using a TSP shortest-path heuristic.
     *  Returns a cycle starting and ending at startVertex, passing through targets.
     * @param zooGraph - graph to operate on
     * @param startVertex - starting and ending point
     * @param targets - list of exhibits to visit, EXCLUDING startVertex
     */
    public GraphRoute(
            Graph<String, IdentifiedWeightedEdge> zooGraph,
            ArrayList<String> targets,
            String startVertex) {

        // Set route's graph
        this.zooGraph = zooGraph;

        // Unvisited targets array keeps track of which vertices to find a path to
        ArrayList<String> unvisitedTargets = new ArrayList<String>(targets);

        // Create DSP object to compute path costs
        DijkstraShortestPath<String, IdentifiedWeightedEdge> dijkstraShortestPath
                = new DijkstraShortestPath<String, IdentifiedWeightedEdge>(zooGraph);

        String currVertex = startVertex;
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
        totalWeight += currPath.getWeight();

        this.pathEdges = new GraphWalk<String, IdentifiedWeightedEdge>(
                zooGraph, startVertex, startVertex, totalEdges, totalWeight);
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
     *  Does NOT include startVertex.
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
}
