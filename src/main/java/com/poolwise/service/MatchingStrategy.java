package com.poolwise.service;

import com.poolwise.model.*;
import java.util.List;

/**
 * Interface for carpool matching algorithms.
 * OOP: Interface, Strategy Pattern — different algorithms can be swapped in.
 */
public interface MatchingStrategy {
    /**
     * Match passengers to drivers and return carpool groups.
     * @param drivers        list of available drivers
     * @param passengers     list of passengers to match
     * @param mode           optimization mode ("MIN_CARS" or "BALANCED")
     * @param carpoolIdStart starting number for naming carpool IDs
     */
    List<Carpool> match(List<Driver> drivers, List<Passenger> passengers,
                        String mode, int carpoolIdStart);

    /** Compute compatibility score [0.0 – 1.0] between one driver and one passenger. */
    double computeScore(Driver driver, Passenger passenger);
}
