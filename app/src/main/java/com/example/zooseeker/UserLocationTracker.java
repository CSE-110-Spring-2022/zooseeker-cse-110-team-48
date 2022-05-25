package com.example.zooseeker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Object to track user location changes, updating app as a consequence
 *  Uses a location permissions checker to ensure app GPS access
 */
public class UserLocationTracker {
    private Location lastUserLocation;
    /**
     * Auxiliary location observer interface, should be implemented by activities relying on GPS
     */
    public interface LocationObserver {
        void update(Location location);
    }

    // Observers list
    private ArrayList<LocationObserver> observers = new ArrayList<>();

    // Fields to assist in location updating and tracking
    private String provider;
    private LocationManager locationManager;
    private final PermissionChecker permissionChecker;
    private ComponentActivity parentActivity;

    /**
     * Ctor for the location tracker.
     * @param activity - Activity which is using the tracker
     */
    public UserLocationTracker(ComponentActivity activity) {
        parentActivity = activity;
        permissionChecker = new PermissionChecker(parentActivity);
        if (permissionChecker.ensurePermissions()) return;

        provider = LocationManager.GPS_PROVIDER;
        locationManager = (LocationManager) parentActivity.getSystemService(Context.LOCATION_SERVICE);

        // Create listener which prompts update logic when location changes
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d("ZooSeeker", String.format("Location changed: %s", location));

                onZooLocationChanged(location);
            }
        };

        // Throw SecurityException if (for some reason) location tracking is not permitted
        if (ActivityCompat.checkSelfPermission(parentActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(parentActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("App need fine and course locations for user tracking!");
        }
        locationManager.requestLocationUpdates(provider, 0, 0.0f, locationListener);
        lastUserLocation = locationManager.getLastKnownLocation(provider);

    }

    /**
     * Add observers to this tracker
     * @param o - observer to update locations for
     */
    public void registerObserver(LocationObserver o) {
        observers.add(o);
    }

    /**
     * Remove observers from this tracker
     * @param o - observer to remove
     */
    public void removeObserver(LocationObserver o) {
        observers.remove(o);
    }

    /**
     * Called when user location changes. Perform updates to observers if so.
     *  *Serves as observer notifier
     * @param location - User's new location
     */
    private void onZooLocationChanged(Location location) {
        lastUserLocation = location;
        for (LocationObserver o : observers) {
            o.update(location);
        }
    }

    /**
     * Returns the last known user location
     * @return Location of last user gps hit
     */
    public Location getUserLocation() {
        return lastUserLocation;
    }

    /**
     * Returns the name of the closest exhibit to the current user location.
     * @param location - Location of user
     * @param vertexInfoHashMap - map of zoo locations and info
     * @return Name of the exhibit closest to user, in id form
     */
    public static String nearestExhibit(
            Location location, HashMap<String, ZooData.VertexInfo> vertexInfoHashMap) {
        Set<String> zooLocations = vertexInfoHashMap.keySet();

        String nearest = "";
        double nearestDist = Double.MAX_VALUE;

        for (String zooLocation : zooLocations) {
            ZooData.VertexInfo currVertex = vertexInfoHashMap.get(zooLocation);
            double currDist = distance(getExhibitLocation(currVertex), location);
            if (currDist < nearestDist) {
                nearest = currVertex.id;
                nearestDist = currDist;
            }
        }

        return nearest;
    }

    /**
     * Returns a Location of an exhibit in vertexInfo format
     */
    public static Location getExhibitLocation(ZooData.VertexInfo vertexInfo) {
        Location exhibitLocation = new Location("");
        exhibitLocation.setLatitude(vertexInfo.lat);
        exhibitLocation.setLongitude(vertexInfo.lng);

        return exhibitLocation;
    }

    /**
     * Returns the approximate distance in feet between locations a and b
     * @return distance between a and b in feet
     */
    public static double distance(Location a, Location b) {
        return a.distanceTo(b) * 3.28084;
    }
}
