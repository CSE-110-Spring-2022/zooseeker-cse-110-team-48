package com.example.zooseeker;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;

import java.util.ArrayList;

/**\
 * This class uses a shortest-path TSP heuristic to find a short cycle, given vertices to visit.
 */
public class GraphRouteFinder {
    /**
     * TSP shortest-path heuristic. Returns a cycle starting and ending at startVertex,
     *  passing through targets.
     * @param zooGraph - graph to operate on
     * @param startVertex - starting and ending point
     * @param targets - list of exhibits to visit, EXCLUDING startVertex
     * @return Path emulating TSP solution
     */
    public static GraphPath<String, IdentifiedWeightedEdge> findRoute(
            Graph<String, IdentifiedWeightedEdge> zooGraph,
            ArrayList<String> targets,
            String startVertex) {

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

            // Update current exhibit
            currVertex = closestVertex;
            unvisitedTargets.remove(closestVertex);
        }

        // Return to startVertex
        currPath = dijkstraShortestPath.getPath(currVertex, startVertex);
        for (IdentifiedWeightedEdge edge : currPath.getEdgeList()) {
            totalEdges.add(edge);
        }
        totalWeight += currPath.getWeight();

        return new GraphWalk<String, IdentifiedWeightedEdge>(zooGraph, startVertex, startVertex, totalEdges, totalWeight);
    }
}
