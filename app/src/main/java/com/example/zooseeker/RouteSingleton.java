package com.example.zooseeker;

/**
 * Route singleton class, designed to allow all activities in the app to access the zoo route obj
 */
public class RouteSingleton {

    private static Route ROUTE = null;

    private RouteSingleton(){};

    /**
     * Get instance of route, callable from any activity
     * @return
     */
    public static Route getRoute() {
        return ROUTE;
    }

    /**
     * Set instance of route from any activity
     * @param route
     */
    public static void setRoute(Route route) {
        ROUTE = route;
    }
}
