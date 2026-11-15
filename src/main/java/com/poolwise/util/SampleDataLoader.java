package com.poolwise.util;

import com.poolwise.exception.InvalidUserException;
import com.poolwise.model.*;
import com.poolwise.service.UserService;
import java.time.LocalTime;

/**
 * Loads realistic sample data for demonstration.
 * 4 drivers, 8 passengers — Delhi NCR locations.
 * OOP: Demonstrates StringBuilder, static utility, exception handling.
 */
public class SampleDataLoader {

    public static String load(UserService svc) {
        StringBuilder log = new StringBuilder("Loading sample data...\n");
        svc.clearAllMatches();

        // ── DRIVERS ─────────────────────────────────────────────────
        addDriver(svc, log, "D001","Rahul Sharma","9876543210",0.92,
            "Connaught Place",28.6139,77.2090,"IIT Delhi",28.5459,77.1926,
            LocalTime.of(8,0),3,5.0,"DL01AB1234","Honda","City","White");

        addDriver(svc, log, "D002","Arjun Mehta","9123456780",0.88,
            "Kashmiri Gate",28.6353,77.2250,"IIT Delhi",28.5459,77.1926,
            LocalTime.of(8,15),2,4.0,"DL02CD5678","Maruti","Swift","Silver");

        addDriver(svc, log, "D003","Mehak Kapoor","9988776655",0.95,
            "AIIMS",28.5921,77.2219,"IIT Delhi",28.5459,77.1926,
            LocalTime.of(8,10),3,6.0,"DL03EF9012","Toyota","Innova","Grey");

        addDriver(svc, log, "D004","Priya Singh","9765432198",0.78,
            "Ghaziabad",28.6692,77.4538,"IIT Delhi",28.5459,77.1926,
            LocalTime.of(7,30),2,3.0,"UP14GH3456","Hyundai","i20","Blue");

        // ── PASSENGERS ───────────────────────────────────────────────
        addPassenger(svc, log,"P001","Amit Kumar",  "9811223344",0.90,"Karol Bagh",   28.6519,77.1902,"IIT Delhi",28.5459,77.1926,LocalTime.of(9,30),20,4.0);
        addPassenger(svc, log,"P002","Sneha Patel", "9922334455",0.85,"Rajouri Garden",28.6485,77.1225,"IIT Delhi",28.5459,77.1926,LocalTime.of(9,30),25,5.0);
        addPassenger(svc, log,"P003","Vikram Nair", "9733445566",0.88,"Lajpat Nagar", 28.5706,77.2434,"IIT Delhi",28.5459,77.1926,LocalTime.of(9,30),15,3.0);
        addPassenger(svc, log,"P004","Priti Verma", "9644556677",0.82,"Saket",        28.5244,77.2167,"IIT Delhi",28.5459,77.1926,LocalTime.of(9,30),20,4.0);
        addPassenger(svc, log,"P005","Rohan Gupta", "9555667788",0.79,"Dwarka",       28.5823,77.0500,"IIT Delhi",28.5459,77.1926,LocalTime.of(9,30),30,6.0);
        addPassenger(svc, log,"P006","Nisha Reddy", "9466778899",0.91,"Vasant Kunj",  28.5244,77.1500,"IIT Delhi",28.5459,77.1926,LocalTime.of(9,30),20,5.0);
        addPassenger(svc, log,"P007","Deepak Joshi","9377889900",0.86,"Kashmiri Gate",28.6353,77.2250,"IIT Delhi",28.5459,77.1926,LocalTime.of(9,15),15,3.0);
        addPassenger(svc, log,"P008","Anjali Singh","9288990011",0.83,"Karol Bagh",   28.6519,77.1902,"IIT Delhi",28.5459,77.1926,LocalTime.of(9,30),25,4.0);

        log.append("\n✓ Loaded ").append(svc.driverCount()).append(" drivers, ")
           .append(svc.passengerCount()).append(" passengers.");
        return log.toString();
    }

    private static void addDriver(UserService svc, StringBuilder log, String id, String name, String phone, double rel,
            String sLoc, double sLat, double sLon, String dLoc, double dLat, double dLon,
            LocalTime dep, int seats, double detour, String reg, String make, String model, String color) {
        try {
            svc.addDriver(new Driver(id, name, phone, rel, sLoc, sLat, sLon, dLoc, dLat, dLon, dep, seats, detour,
                new Vehicle(reg, make, model, color, seats)));
            log.append("  ✓ Driver: ").append(name).append("\n");
        } catch (Exception e) { log.append("  ✗ ").append(name).append(": ").append(e.getMessage()).append("\n"); }
    }

    private static void addPassenger(UserService svc, StringBuilder log, String id, String name, String phone, double rel,
            String pLoc, double pLat, double pLon, String dLoc, double dLat, double dLon,
            LocalTime arr, double wait, double detour) {
        try {
            svc.addPassenger(new Passenger(id, name, phone, rel, pLoc, pLat, pLon, dLoc, dLat, dLon, arr, wait, detour));
            log.append("  ✓ Passenger: ").append(name).append("\n");
        } catch (Exception e) { log.append("  ✗ ").append(name).append(": ").append(e.getMessage()).append("\n"); }
    }
}
