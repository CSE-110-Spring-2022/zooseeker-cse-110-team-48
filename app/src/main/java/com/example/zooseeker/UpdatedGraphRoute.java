package com.example.zooseeker;

import android.location.Location;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.GraphWalk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**\
 * This class represents an exhibit route, plotted using a TSP heuristic
 */
public class UpdatedGraphRoute implements Serializable {

    // Fields for graph properties
    AbstractBaseGraph<String, IdentifiedWeightedEdge> zooGraph;
    HashMap<String, ZooData.VertexInfo> vertexInfo;
    HashMap<String, ZooData.EdgeInfo> edgeInfo;

    String startVertex;
    String currStreet; // Street the user is at
    GraphWalk<String, IdentifiedWeightedEdge> pathEdges;
    ArrayList<String> exhibitOrder = new ArrayList<>(); // Order of exhibits of interest
    ArrayList<IdentifiedWeightedEdge> edgeList;
    HashMap<String, Double> exhibitDistances = new HashMap<>();
    DijkstraShortestPath<String, IdentifiedWeightedEdge> dijkstraShortestPath;

    // Indexes the progress of user in the route, in terms of exhibit indices in exhibitOrder
    private int nextExhibitIndex = 0;

    private int currentEdgeIndex = 0; // Edge index the user is at while traversing the route

    /**
     * Route constructor, using a TSP shortest-path heuristic.
     *  Returns a cycle starting and ending at startVertex, passing through targets.
     * @param zooGraph - graph to operate on
     * @param startVertex - starting and ending point
     * @param targets - list of exhibits to visit, EXCLUDING startVertex
     */
    public UpdatedGraphRoute(
        AbstractBaseGraph<String, IdentifiedWeightedEdge> zooGraph,
        HashMap<String, ZooData.VertexInfo> vertexInfo,
        HashMap<String, ZooData.EdgeInfo> edgeInfo,
        ArrayList<String> targets,
        String startVertex) {

        // Set route's graph fields and start vertex
        this.zooGraph = zooGraph;
        this.vertexInfo = vertexInfo;
        this.edgeInfo = edgeInfo;
        this.startVertex = startVertex;

        // Unvisited targets array keeps track of which vertices to find a path to
        ArrayList<String> unvisitedTargets = new ArrayList<>(targets);

        // Create DSP object to compute path costs
        this.dijkstraShortestPath =  new DijkstraShortestPath<>(zooGraph);

        // Add the starting point to exhibit order, and create objects to track route creation
        String currVertex = startVertex;
        exhibitOrder.add(startVertex);
        GraphPath<String, IdentifiedWeightedEdge> currPath;
        ArrayList<IdentifiedWeightedEdge> totalEdges = new ArrayList<>();
        double totalWeight = .0;

        // Loop through all exhibits of interest until we visit all of them
        while (unvisitedTargets.size() > 0) {
            // Temp variables until closest exhibit is found
            String closestVertex = unvisitedTargets.get(0);
            double closestPathWeight = Double.MAX_VALUE;
            double tempPathWeight;

            // Find the closest vertex
            for (String vertex : unvisitedTargets) {
                String exhibitGroupId = vertexInfo.get(vertex).group_id;
                tempPathWeight = getPathWeight(currVertex, vertex);

                if (tempPathWeight < closestPathWeight) {
                    closestVertex = vertex;
                    closestPathWeight = tempPathWeight;
                }
            }

            // Go to next closest exhibit
            currPath = getPath(currVertex, closestVertex);
            // Add path to total path
            totalEdges.addAll(currPath.getEdgeList());
            totalWeight += currPath.getWeight();

            // Update current exhibit, and add exhibit to route fields
            currVertex = closestVertex;
            exhibitOrder.add(closestVertex);
            exhibitDistances.put(closestVertex, totalWeight);
            unvisitedTargets.remove(closestVertex);
        }

        // Return to startVertex
        currPath = getPath(currVertex, startVertex);
        totalEdges.addAll(currPath.getEdgeList());
        exhibitOrder.add(startVertex);
        totalWeight += currPath.getWeight();

        this.pathEdges = new GraphWalk<>(
                zooGraph, startVertex, startVertex, totalEdges, totalWeight);
        this.edgeList = getEdgeList();
    }

    /**
     * Wrapper method for getPath, taking care of the case where locations are subexhibits of groups
     * @param locationA - first location
     * @param locationB - destination location
     * @return GraphPath from locationA to locationB
     */
    public GraphPath<String, IdentifiedWeightedEdge> getPath(String locationA, String locationB) {
        String exhibitGroupIdA = vertexInfo.get(locationA).group_id;
        String exhibitGroupIdB = vertexInfo.get(locationB).group_id;
        if (exhibitGroupIdA != null) {
            locationA = exhibitGroupIdA;
        }
        if (exhibitGroupIdB != null) {
            locationB = exhibitGroupIdB;
        }
        return dijkstraShortestPath.getPath(locationA, locationB);
    }

    /**
     * Wrapper method for getPathWeight, taking care of the case where locations are subexhibits of groups
     * @param locationA - first location
     * @param locationB - destination location
     * @return double path weight from locationA to locationB
     */
    public double getPathWeight(String locationA, String locationB) {
        String exhibitGroupIdA = vertexInfo.get(locationA).group_id;
        String exhibitGroupIdB = vertexInfo.get(locationB).group_id;
        if (exhibitGroupIdA != null) {
            locationA = exhibitGroupIdA;
        }
        if (exhibitGroupIdB != null) {
            locationB = exhibitGroupIdB;
        }
        return dijkstraShortestPath.getPathWeight(locationA, locationB);
    }

    public String getExhibitOrGroupID(String location) {
        String group_id = vertexInfo.get(location).group_id;
        if (group_id != null) return group_id;
        else return location;
    }

    /**
     * Getter for edge list of route cycle
     * @return list of edges from route cycle
     */
    public ArrayList<IdentifiedWeightedEdge> getEdgeList() {
        return new ArrayList<>(this.pathEdges.getEdgeList());
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
     * @return List of exhibits to visit in order
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
     * @return directions - ArrayList containing human-readable directions to get to the next
     * exhibit
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
        destExhibit = getExhibitOrGroupID(destExhibit);

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
        return (zooGraph.getEdgeTarget(e).equals(exhibit) || zooGraph.getEdgeSource(e).equals(
                exhibit));
    }

    /**
     * Returns the human-readable directions associated with an edge
     * @param e - edge to convert to text directions
     * @param nextLocation - destination vertex of edge e
     * @param prevStreet - street of previous direction. Needed to determine continue or proceed
     * @return result - string direction for output
     */
    public String edgeToDirection(IdentifiedWeightedEdge e, String nextLocation, String prevStreet)
    {
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
        if (vertexInfo.get(nextLocation).kind.toString().equals("EXHIBIT")) {
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

    public static String condenseDirectionsList(ArrayList<String> list) {
        StringBuilder finalString = new StringBuilder();
        for (String s : list) {
            finalString.append(s).append("\n");
        }
        return finalString.toString();
    }

    /**
     * Changes the route based on the user's current location. Assumes s/he has traveled a short
     * distance, for now.
     *
     * @param userLocation User's current location
     * @return ArrayList containing human-readable directions to get to the next exhibit
     */
    public String update(Location userLocation) {
        ArrayList<String> directions = new ArrayList<>();
        if (reachedEnd()) {
            return "End of route";
        }

        String currentVertex = exhibitOrder.get(nextExhibitIndex);
        final String destExhibit = exhibitOrder.get(nextExhibitIndex + 1);
        String nextVertex;
        ZooData.VertexInfo currentVertexInfo;
        String closestVertex = currentVertex;
        Location vertexLocation = new Location("UpdatedGraphRoute.update");
        int edgeIndex = this.currentEdgeIndex;
        float minDistance = Float.MAX_VALUE;
        float currentDistance;
        boolean reachedNextLocation = false;

        /* Finds closest vertex */
        {
            // Traverse edges, until after we encounter an edge with the destExhibit vertex
            do {
                IdentifiedWeightedEdge currentEdge = edgeList.get(edgeIndex++);
                currentVertexInfo = this.vertexInfo.get(currentVertex);
                vertexLocation.setLatitude(currentVertexInfo.lat);
                vertexLocation.setLongitude(currentVertexInfo.lng);
                currentDistance = userLocation.distanceTo(vertexLocation);
                if (currentDistance < minDistance) {
                    minDistance = currentDistance;
                    closestVertex = currentVertex;
                }
                if (edgeHasExhibit(currentEdge, destExhibit)) {
                    reachedNextLocation = true;
                }

                // Whichever vertex is not the current location, is the next vertex to visit
                String source = zooGraph.getEdgeSource(currentEdge);
                String target = zooGraph.getEdgeTarget(currentEdge);
                if (source.equals(currentVertex)) {
                    nextVertex = target;
                } else {
                    nextVertex = source;
                }

                currentVertex = nextVertex;
            } while (!reachedNextLocation);
        }

        /* Makes directions from closest vertex to next exhibit */
        {
            // Gets the current vertex (currentLocation), and next POI (destExhibit)
            currentVertex = closestVertex;
            edgeIndex = this.currentEdgeIndex;
            reachedNextLocation = false;

            // Traverses edges, until after we encounter an edge with the destExhibit vertex
            do {
                IdentifiedWeightedEdge currentEdge = edgeList.get(edgeIndex++);
                if (edgeHasExhibit(currentEdge, destExhibit)) {
                    reachedNextLocation = true;
                }

                // Whichever vertex is not the current one is the next to visit
                String source = zooGraph.getEdgeSource(currentEdge);
                String target = zooGraph.getEdgeTarget(currentEdge);
                if (source.equals(currentVertex)) {
                    nextVertex = target;
                } else {
                    nextVertex = source;
                }

                directions.add(edgeToDirection(currentEdge, nextVertex, currStreet));
                currStreet = edgeInfo.get(currentEdge.getId()).street;
                currentVertex = nextVertex;
            } while (!reachedNextLocation);
        }
        return condenseDirectionsList(directions);
    }
}
