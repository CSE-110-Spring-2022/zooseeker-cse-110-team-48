package com.example.zooseeker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

/**
 * Object to track user location changes, updating app as a consequence
 *  Uses a location permissions checker to ensure app GPS access
 */
public class UserLocationTracker {

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
        locationManager.requestLocationUpdates(provider, 0, 1.0f, locationListener);
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
        // TODO: ADD CODE HERE if logic for nearest exhibit is needed

        for (LocationObserver o : observers) {
            o.update(location);
        }
    }
}
