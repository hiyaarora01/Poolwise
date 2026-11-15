package poolwise;

import java.io.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file I/O for POOLWISE.
 * Format: CSV-style text files.
 * Driver record: id,name,reliability,startLat,startLon,startName,destLat,destLon,destName,hh:mm,seats,maxDetour
 * Passenger record: id,name,reliability,startLat,startLon,startName,destLat,destLon,destName,hh:mm,maxWait
 */
public class DataManager {

    private static final String DATA_DIR = "data/";

    public DataManager() {
        new File(DATA_DIR).mkdirs();
    }

    // ─── SAVE DRIVERS ───
    public void saveDrivers(List<Driver> drivers, String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_DIR + filename))) {
            pw.println("# POOLWISE Driver Data File");
            pw.println("# Format: id,name,reliability,startLat,startLon,startName,destLat,destLon,destName,departure(HH:mm),seats,maxDetourKm");
            for (Driver d : drivers) {
                pw.printf("%s,%s,%.2f,%.6f,%.6f,%s,%.6f,%.6f,%s,%s,%d,%.1f%n",
                    d.getId(), d.getName(), d.getReliabilityScore(),
                    d.getStartLocation().getLatitude(), d.getStartLocation().getLongitude(), d.getStartLocation().getName(),
                    d.getDestination().getLatitude(), d.getDestination().getLongitude(), d.getDestination().getName(),
                    d.getDepartureTime().toString(), d.getAvailableSeats(), d.getMaxDetourKm());
            }
        }
        System.out.println("✓ Saved " + drivers.size() + " drivers to " + DATA_DIR + filename);
    }

    // ─── LOAD DRIVERS ───
    public List<Driver> loadDrivers(String filename) throws IOException {
        List<Driver> drivers = new ArrayList<>();
        File f = new File(DATA_DIR + filename);
        if (!f.exists()) { System.out.println("⚠ File not found: " + f.getPath()); return drivers; }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.isBlank()) continue;
                String[] p = line.split(",");
                if (p.length < 12) continue;
                Location start = new Location(p[5], Double.parseDouble(p[3]), Double.parseDouble(p[4]));
                Location dest  = new Location(p[8], Double.parseDouble(p[6]), Double.parseDouble(p[7]));
                drivers.add(new Driver(p[0], p[1], Double.parseDouble(p[2]),
                    start, dest, LocalTime.parse(p[9]),
                    Integer.parseInt(p[10].trim()), Double.parseDouble(p[11].trim())));
            }
        }
        System.out.println("✓ Loaded " + drivers.size() + " drivers from " + filename);
        return drivers;
    }

    // ─── SAVE PASSENGERS ───
    public void savePassengers(List<Passenger> passengers, String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_DIR + filename))) {
            pw.println("# POOLWISE Passenger Data File");
            pw.println("# Format: id,name,reliability,startLat,startLon,startName,destLat,destLon,destName,arrivalBy(HH:mm),maxWaitMin");
            for (Passenger p : passengers) {
                pw.printf("%s,%s,%.2f,%.6f,%.6f,%s,%.6f,%.6f,%s,%s,%.1f%n",
                    p.getId(), p.getName(), p.getReliabilityScore(),
                    p.getStartLocation().getLatitude(), p.getStartLocation().getLongitude(), p.getStartLocation().getName(),
                    p.getDestination().getLatitude(), p.getDestination().getLongitude(), p.getDestination().getName(),
                    p.getRequiredArrivalTime().toString(), p.getMaxWaitMinutes());
            }
        }
        System.out.println("✓ Saved " + passengers.size() + " passengers to " + DATA_DIR + filename);
    }

    // ─── LOAD PASSENGERS ───
    public List<Passenger> loadPassengers(String filename) throws IOException {
        List<Passenger> passengers = new ArrayList<>();
        File f = new File(DATA_DIR + filename);
        if (!f.exists()) { System.out.println("⚠ File not found: " + f.getPath()); return passengers; }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.isBlank()) continue;
                String[] p = line.split(",");
                if (p.length < 11) continue;
                Location start = new Location(p[5], Double.parseDouble(p[3]), Double.parseDouble(p[4]));
                Location dest  = new Location(p[8], Double.parseDouble(p[6]), Double.parseDouble(p[7]));
                passengers.add(new Passenger(p[0], p[1], Double.parseDouble(p[2]),
                    start, dest, LocalTime.parse(p[9]), Double.parseDouble(p[10].trim())));
            }
        }
        System.out.println("✓ Loaded " + passengers.size() + " passengers from " + filename);
        return passengers;
    }

    // ─── SAVE PLAN REPORT ───
    public void savePlanReport(CarpoolPlan plan, String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_DIR + filename))) {
            pw.println("POOLWISE CARPOOL PLAN REPORT");
            pw.println("============================");
            pw.println("Groups Formed   : " + plan.getCarsUsed());
            pw.println("Passengers Served: " + plan.getTotalPassengersServed());
            pw.println("Cars Avoided    : " + plan.getCarsAvoided());
            pw.println("Extra Distance  : " + String.format("%.2f", plan.getTotalExtraDistance()) + " km");
            pw.println("CO2 Saved       : " + String.format("%.2f", plan.getTotalCO2Saved()) + " kg");
            pw.println("Fuel Saved      : " + String.format("%.2f", plan.getTotalFuelSaved()) + " L");
            pw.println();
            for (CarpoolGroup g : plan.getGroups()) {
                pw.println("GROUP " + g.getGroupId());
                pw.println("  Driver    : " + g.getDriver().getName());
                pw.print("  Passengers: ");
                for (Passenger p : g.getPassengers()) pw.print(p.getName() + " ");
                pw.println();
                pw.println("  Extra km  : " + String.format("%.2f", g.getTotalExtraDistanceKm()));
                pw.println();
            }
            if (!plan.getUnassignedPassengers().isEmpty()) {
                pw.println("UNASSIGNED:");
                for (Passenger p : plan.getUnassignedPassengers()) pw.println("  " + p.getName());
            }
        }
        System.out.println("✓ Plan report saved to " + DATA_DIR + filename);
    }
}
