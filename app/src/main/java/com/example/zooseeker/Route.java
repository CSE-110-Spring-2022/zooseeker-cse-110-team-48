package com.example.zooseeker;

import android.location.Location;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.Map;

/**
 * This class represents a zoo route, using a array
 */
public class Route {
    /**
     * Simple class to represent exhibits in Route
     * Expects location of exhibit (or group if applicable) and metadata
     */
    public class Waypoint {
        public String id;
        public String name;
        public String groupName;
        public Location location;
        public boolean visited;

        /**
         * Constructor for waypoint
         * @param id - id of exhibit
         * @param name - Name of exhibit
         * @param groupName - Name of the exhibit group of waypoint, null if none
         * @param location - Location of exhibit with lat and long
         */
        public Waypoint(String id, String name, String groupName, Location location) {
            this.id = id;
            this.name = name;
            this.groupName = groupName;
            this.location = location;
            this.visited = false;
        }

        // Getters
        public String getId() {return this.id;}
        public String getName() {return this.name;}
        public Location getLocation() {return this.location;}

        /**
         * Set waypoint visited or not
         * @param visited - true to set waypoint to visited, false to set unvisited
         */
        public void setVisited(boolean visited) {
            this.visited = visited;
        }
    }

    // Fields for routing
    public ArrayList<Waypoint> routeOrder = new ArrayList<>();
    private DijkstraShortestPath dijkstraShortestPath;

    // Zoo data objects
    private Graph<String, IdentifiedWeightedEdge> zooGraph;
    private Map<String, ZooData.VertexInfo> exhibitInfo;
    private Map<String, ZooData.EdgeInfo> trailInfo;

    /**
     * Constructs a Route based on a zoo graph reader with access to zoo .json files,
     *  a list of exhibits to visit, and a starting location
     * @param graphReader - DataFilesReader with access to json files
     * @param exhibitIds - List of exhibits to visit
     * @param startId - Starting location of user
     */
    public Route(DataFilesReader graphReader, ArrayList<String> exhibitIds, String startId) {
        // Get graph objects
        this.zooGraph = graphReader.getGraph();
        this.exhibitInfo = graphReader.getVertexInfo();
        this.trailInfo = graphReader.getEdgeInfo();

        // Unvisited exhibits array keeps track of which exhibits to find a path to
        ArrayList<String> unvisitedExhibitIds = new ArrayList<>(exhibitIds);

        // Create DSP object to compute path costs
        this.dijkstraShortestPath =  new DijkstraShortestPath<>(zooGraph);

        // Create starting waypoint based on user location, and add to route ordering
        String currExhibitId = startId;

        // Loop through all exhibits of interest until we visit all of them
        while(unvisitedExhibitIds.size() > 0) {
            String nextExhibitId = closestExhibitId(currExhibitId, unvisitedExhibitIds);
            routeOrder.add(getWaypoint(nextExhibitId));

            // Advance to next exhibit, remove from consideration
            currExhibitId = nextExhibitId;
            unvisitedExhibitIds.remove(nextExhibitId);
        }

        // Add exit gate waypoint to end of route order
        this.routeOrder.add(getWaypoint("entrance_exit_gate"));
    }

    /**
     * Constructs and returns a waypoint object from exhibit/location ID
     * @param locationId - id of exhibit
     * @return Waypoint object representing location
     */
    public Waypoint getWaypoint (String locationId) {
        Location waypointLocation = new Location("");

        // Special case: waypoint has group_id
        String group_id = exhibitInfo.get(locationId).group_id;
        if (group_id != null) {
            waypointLocation.setLatitude(exhibitInfo.get(group_id).lat);
            waypointLocation.setLongitude(exhibitInfo.get(group_id).lng);
        } else {
            waypointLocation.setLatitude(exhibitInfo.get(locationId).lat);
            waypointLocation.setLongitude(exhibitInfo.get(locationId).lng);
        }

        Waypoint waypoint= new Waypoint(
                exhibitInfo.get(locationId).id,
                exhibitInfo.get(locationId).name,
                exhibitInfo.get(locationId).group_id,
                waypointLocation);

        return waypoint;
    }

    /**
     * Returns the closest exhibit to the startId location
     * @param startId - location to consider distances from
     * @param exhibitsToConsider - list of location id's to consider distances to
     * @return result - id of nearest location to startId
     */
    public String closestExhibitId(String startId, ArrayList<String> exhibitsToConsider) {
        String result = exhibitsToConsider.get(0);
        double resultPathWeight = Double.MAX_VALUE;
        double tempPathWeight;

        // Find the closest vertex to startId's location
        for (String vertex : exhibitsToConsider) {
            tempPathWeight = getPathWeight(startId, vertex);

            if (tempPathWeight < resultPathWeight) {
                result = vertex;
                resultPathWeight = tempPathWeight;
            }
        }
        return result;
    }

    /**
     * Wrapper method for getPath, taking care of the case where locations are subexhibits of groups
     * @param startId - first location
     * @param endId - destination location
     * @return GraphPath from locationA to locationB
     */
    public GraphPath<String, IdentifiedWeightedEdge> getPath(String startId, String endId) {
        String exhibitGroupIdA = exhibitInfo.get(startId).group_id;
        String exhibitGroupIdB = exhibitInfo.get(endId).group_id;
        if (exhibitGroupIdA != null) {
            startId = exhibitGroupIdA;
        }
        if (exhibitGroupIdB != null) {
            endId = exhibitGroupIdB;
        }
        return dijkstraShortestPath.getPath(startId, endId);
    }

    /**
     * Wrapper method for getPathWeight, taking care of the case where locations are subexhibits of groups
     * @param startId - first location
     * @param endId - destination location
     * @return double path weight from locationA to locationB
     */
    public double getPathWeight(String startId, String endId) {
        String exhibitGroupIdA = exhibitInfo.get(startId).group_id;
        String exhibitGroupIdB = exhibitInfo.get(endId).group_id;
        if (exhibitGroupIdA != null) {
            startId = exhibitGroupIdA;
        }
        if (exhibitGroupIdB != null) {
            endId = exhibitGroupIdB;
        }
        return dijkstraShortestPath.getPathWeight(startId, endId);
    }

    /**
     * Returns the total distance from the start of route to exhibitId's exhibit
     * @param exhibitId - exhibit to go to
     * @param startId - start of route
     * @return distance from start to exhibit
     */
    public double getTotalDistance(String exhibitId, String startId) {
        double totalDistance = 0.0;
        int currExhibitIndex = 0;

        totalDistance += getPathWeight(startId, routeOrder.get(currExhibitIndex).id);
        // Loop until exhibitId is encountered in route order
        while (!routeOrder.get(currExhibitIndex).id.equals(exhibitId)) {
            // Add distance between current waypoint and next waypoint
            totalDistance += getPathWeight(
                    routeOrder.get(currExhibitIndex).id,
                    routeOrder.get(currExhibitIndex + 1).id);

            currExhibitIndex++;
        }
        return totalDistance;
    }

    /**
     * Checks if we have reached the end of route
     * @return true if route ended
     */
    public boolean reachedEnd() {
        return routeOrder.get(routeOrder.size() - 1).visited;
    }

    /**
     * Get the next exhibit/waypoint index
     * @return index of next waypoint in route order array
     */
    public int getNextExhibitIndex() {
        int currWaypointIndex = 0;

        // Loops until unvisited waypoint, returns index of next exhibit to visit
        while (routeOrder.get(currWaypointIndex).visited) {
            currWaypointIndex++;
        }
        return currWaypointIndex;
    }

    /**
     * Advances route to next exhibit, returning directions to go there from user location
     * @param startId: id of starting location for directions
     * @return List of Strings representing directions to next exhibit
     */
    public ArrayList<String> advanceToNextExhibit(String startId) {
        Waypoint nextWaypoint = routeOrder.get(getNextExhibitIndex());

        ArrayList<String> directionsList = getDirections(startId, nextWaypoint.id);

        // Update current waypoint to be visited already
        nextWaypoint.setVisited(true);

        return directionsList;
    }

    /**
     * Reverses route to last exhibit visited, giving backtrack directions
     * @return List of Strings representing directions to last exhibit
     */
    public ArrayList<String> returnToPreviousExhibit(String startId) {
        Waypoint lastVisitedWaypoint = routeOrder.get(getNextExhibitIndex() - 1);

        ArrayList<String> directionsList = getDirections(startId, lastVisitedWaypoint.id);

        lastVisitedWaypoint.setVisited(false);

        return directionsList;
    }

    /**
     * Gets arraylist of directions from a starting location to ending location
     *  Assumes that the start and end are joined by Dijkstra pathway
     * @param startId - starting location id
     * @param endId - ending exhibit id
     * @return ArrayList of directions from start to end locations
     */
    public ArrayList<String> getDirections(String startId, String endId) {
        ArrayList<String> directionsList = new ArrayList<>();

        // Get path of travel
        GraphPath<String, IdentifiedWeightedEdge> path = getPath(startId, endId);

        String currLocationId = startId;
        String nextLocationId;
        String prevStreet = null;
        // Generate directions for each path
        for (IdentifiedWeightedEdge e : path.getEdgeList()) {
            // Disambiguate next location's id (due to undirected graph)
            if (currLocationId.equals(zooGraph.getEdgeSource(e))) {
                nextLocationId = zooGraph.getEdgeTarget(e);
            } else {
                nextLocationId = zooGraph.getEdgeSource(e);
            }

            // Get directions and update current location ID
            directionsList.add(edgeToDirection(e, nextLocationId, prevStreet));
            currLocationId = nextLocationId;
            prevStreet = trailInfo.get(e.getId()).street;
        }

        return directionsList;
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
        String street = trailInfo.get(e.getId()).street;
        double distance = zooGraph.getEdgeWeight(e);
        String destination = (exhibitInfo.get(nextLocation).name);

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
        if (exhibitInfo.get(nextLocation).kind.toString().equals("EXHIBIT")) {
            result = result + " Exhibit";
        }
        return result;
    }

    /**
     * Rerouting algorithm which reorders remaining exhibits to visit
     * @param startId - Id of nearest zoo location to user
     */
    public void reroute(String startId) {
        // Unvisited exhibits array keeps track of which exhibits to find a path to
        ArrayList<String> unvisitedExhibitIds = new ArrayList<>();

        // Get id's of all unvisited exhibits of interest
        for (Waypoint unvisitedWaypoint :
                routeOrder.subList(getNextExhibitIndex(), routeOrder.size() - 1)) {
            unvisitedExhibitIds.add(unvisitedWaypoint.id);
        }

        // Remove all unvisited exhibits from route list, to be added later in different order
        while (!routeOrder.get(routeOrder.size() - 1).visited) {
            routeOrder.remove(routeOrder.size() - 1);
        }

        String currExhibitId = startId;

        // Loop through all exhibits of interest until we visit all of them
        while(unvisitedExhibitIds.size() > 0) {
            String nextExhibitId = closestExhibitId(currExhibitId, unvisitedExhibitIds);
            routeOrder.add(getWaypoint(nextExhibitId));

            // Advance to next exhibit, remove from consideration
            currExhibitId = nextExhibitId;
            unvisitedExhibitIds.remove(nextExhibitId);
        }

        // Re-add gate waypoint to end of route order
        this.routeOrder.add(getWaypoint("entrance_exit_gate"));
    }

    /**
     * Checks if user is off-track
     * @param startId - id of location closest to user
     * @return true if closest exhibit to visit is no longer next exhibit to visit
     */
    public boolean isOffTrack(String startId) {
        Waypoint nextExhibit = routeOrder.get(getNextExhibitIndex());
        double distToNextExhibit = getPathWeight(startId, nextExhibit.id);

        for (int otherExhibitIndex = getNextExhibitIndex() + 1;
             otherExhibitIndex < routeOrder.size();
             otherExhibitIndex ++) {
            if (distToNextExhibit > getPathWeight(startId, routeOrder.get(otherExhibitIndex).id)) return true;
        }
        return false;
    }

    /**
     * Removes next exhibit from consideration, replanning afterwards
     * @param startId - location nearest to user
     */
    public void skipNextExhibit(String startId) {
        routeOrder.remove(getNextExhibitIndex());
        reroute(startId);
    }

}
