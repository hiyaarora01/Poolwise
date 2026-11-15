package com.poolwise.service;

import com.poolwise.model.*;
import java.util.List;

/**
 * Interface for route optimization strategies.
 * OOP: Interface, abstraction.
 */
public interface RouteOptimizerInterface {
    /** Return ordered list of location names for the pickup route. */
    List<String> optimizeRoute(Driver driver, List<Passenger> passengers);

    /** Calculate total route distance (km) for a given sequence of location names. */
    double calculateRouteDistance(List<String> routeNodes);
}
