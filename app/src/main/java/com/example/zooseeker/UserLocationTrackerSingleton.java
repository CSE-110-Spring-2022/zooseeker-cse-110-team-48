package com.example.zooseeker;

/**
 * Route singleton class, designed to allow all activities in the app to access user locations
 */
public class UserLocationTrackerSingleton {

    private static UserLocationTracker TRACKER = null;

    private UserLocationTrackerSingleton(){};

    public static UserLocationTracker getTracker() {
        return TRACKER;
    }

    public static void setTracker(UserLocationTracker tracker) {
        TRACKER = tracker;
    }
}
