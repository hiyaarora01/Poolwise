package com.poolwise.service;

import com.poolwise.model.*;
import java.util.List;

/**
 * Calculates the environmental impact of carpooling vs individual travel.
 *
 * ASSUMPTIONS (documented):
 *   Fuel efficiency : 12 km/L (average Indian petrol car)
 *   CO2 per litre   : 2.31 kg  (petrol combustion factor)
 *   CO2 per km      : 2.31/12  = ~0.1925 kg/km
 *   Fuel cost       : ₹102/L   (approximate 2024 Delhi petrol price)
 *
 * All results are ESTIMATES — actual emissions vary with vehicle, load, traffic.
 */
public class ImpactCalculator {

    public static final double FUEL_EFF_KM_PER_L  = 12.0;
    public static final double CO2_KG_PER_LITRE   = 2.31;
    public static final double CO2_KG_PER_KM      = CO2_KG_PER_LITRE / FUEL_EFF_KM_PER_L;
    public static final double FUEL_COST_PER_LITRE = 102.0;

    public record ImpactReport(
        int    totalPassengers,
        int    passengersMatched,
        int    carsWithoutCarpool,
        int    carsWithCarpool,
        int    carsAvoided,
        double totalDistanceSavedKm,
        double totalExtraDistanceKm,
        double fuelSavedLitres,
        double co2SavedKg,
        double costSavedRupees
    ) {}

    public static ImpactReport calculate(List<Carpool> carpools,
                                         List<Driver>    allDrivers,
                                         List<Passenger> allPassengers) {
        int totalPassengers     = allPassengers.size();
        int passengersMatched   = carpools.stream().mapToInt(Carpool::getPassengerCount).sum();
        int carsWithoutCarpool  = totalPassengers + allDrivers.size();
        int carsWithCarpool     = carpools.size() + allDrivers.size();
        int carsAvoided         = passengersMatched; // each matched passenger avoids a solo car

        double avgDirectKm      = allDrivers.stream().mapToDouble(Driver::directDistanceKm).average().orElse(10.0);
        double distSaved        = carsAvoided * avgDirectKm;
        double extraDist        = carpools.stream().mapToDouble(Carpool::getExtraDistanceKm).sum();
        double netDistSaved     = Math.max(0, distSaved - extraDist);
        double fuelSaved        = netDistSaved / FUEL_EFF_KM_PER_L;
        double co2Saved         = fuelSaved * CO2_KG_PER_LITRE;
        double costSaved        = fuelSaved * FUEL_COST_PER_LITRE;

        return new ImpactReport(
            totalPassengers, passengersMatched,
            carsWithoutCarpool, carsWithCarpool, carsAvoided,
            netDistSaved, extraDist, fuelSaved, co2Saved, costSaved
        );
    }
}
