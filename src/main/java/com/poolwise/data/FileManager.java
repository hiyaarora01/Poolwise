package com.poolwise.data;

import com.poolwise.model.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.*;

/**
 * Handles all file I/O for persistent storage.
 * OOP: File handling (BufferedReader, PrintWriter, NIO), exception handling.
 *
 * File format: CSV with comment lines starting with '#'
 */
public class FileManager {

    private static final Logger LOG  = Logger.getLogger(FileManager.class.getName());
    public  static final String DATA_DIR = "data/";
    private static final String DRIVERS_FILE    = DATA_DIR + "drivers.csv";
    private static final String PASSENGERS_FILE = DATA_DIR + "passengers.csv";
    private static final String CARPOOLS_FILE   = DATA_DIR + "carpools.txt";

    public FileManager() {
        try { Files.createDirectories(Paths.get(DATA_DIR)); }
        catch (IOException e) { LOG.warning("Could not create data dir: " + e.getMessage()); }
    }

    // ── SAVE ──────────────────────────────────────────────────────

    public boolean saveDrivers(List<Driver> drivers) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DRIVERS_FILE))) {
            pw.println("# POOLWISE Drivers CSV — do not edit manually");
            pw.println("# userId,name,phone,reliability,startLoc,startLat,startLon,dest,destLat,destLon,departure,seats,maxDetour,available,regNum,make,model,color,capacity");
            for (Driver d : drivers) pw.println(d.toCsv());
            LOG.info("Saved " + drivers.size() + " drivers.");
            return true;
        } catch (IOException e) { LOG.severe("Save drivers failed: " + e.getMessage()); return false; }
    }

    public boolean savePassengers(List<Passenger> passengers) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PASSENGERS_FILE))) {
            pw.println("# POOLWISE Passengers CSV");
            pw.println("# userId,name,phone,reliability,pickupLoc,pickupLat,pickupLon,dest,destLat,destLon,arrivalTime,maxWait,maxDetour,available,assignedDriverId");
            for (Passenger p : passengers) pw.println(p.toCsv());
            LOG.info("Saved " + passengers.size() + " passengers.");
            return true;
        } catch (IOException e) { LOG.severe("Save passengers failed: " + e.getMessage()); return false; }
    }

    public boolean saveCarpools(List<Carpool> carpools) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CARPOOLS_FILE))) {
            pw.println("# POOLWISE Carpool Arrangements");
            pw.println("# Generated: " + java.time.LocalDateTime.now());
            pw.println();
            for (Carpool cp : carpools) { pw.println(cp.toString()); pw.println("---"); }
            return true;
        } catch (IOException e) { LOG.severe("Save carpools failed: " + e.getMessage()); return false; }
    }

    // ── LOAD ──────────────────────────────────────────────────────

    public List<Driver> loadDrivers() {
        List<Driver> result = new ArrayList<>();
        File f = new File(DRIVERS_FILE);
        if (!f.exists()) return result;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.isBlank()) continue;
                try { Driver d = parseDriver(line); if (d != null) result.add(d); }
                catch (Exception e) { LOG.warning("Bad driver record: " + e.getMessage()); }
            }
        } catch (IOException e) { LOG.severe("Load drivers failed: " + e.getMessage()); }
        LOG.info("Loaded " + result.size() + " drivers.");
        return result;
    }

    public List<Passenger> loadPassengers() {
        List<Passenger> result = new ArrayList<>();
        File f = new File(PASSENGERS_FILE);
        if (!f.exists()) return result;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.isBlank()) continue;
                try { Passenger p = parsePassenger(line); if (p != null) result.add(p); }
                catch (Exception e) { LOG.warning("Bad passenger record: " + e.getMessage()); }
            }
        } catch (IOException e) { LOG.severe("Load passengers failed: " + e.getMessage()); }
        LOG.info("Loaded " + result.size() + " passengers.");
        return result;
    }

    // ── Parsers ───────────────────────────────────────────────────

    private Driver parseDriver(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 14) return null;
        Vehicle v = null;
        if (p.length >= 19 && !p[14].isBlank())
            v = new Vehicle(p[14].trim(), p[15].trim(), p[16].trim(), p[17].trim(), Integer.parseInt(p[18].trim()));
        Driver d = new Driver(
            p[0].trim(), p[1].trim(), p[2].trim(), Double.parseDouble(p[3].trim()),
            p[4].trim(), Double.parseDouble(p[5].trim()), Double.parseDouble(p[6].trim()),
            p[7].trim(), Double.parseDouble(p[8].trim()), Double.parseDouble(p[9].trim()),
            LocalTime.parse(p[10].trim()),
            Integer.parseInt(p[11].trim()), Double.parseDouble(p[12].trim()), v);
        d.setAvailable(Boolean.parseBoolean(p[13].trim()));
        return d;
    }

    private Passenger parsePassenger(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 14) return null;
        Passenger ps = new Passenger(
            p[0].trim(), p[1].trim(), p[2].trim(), Double.parseDouble(p[3].trim()),
            p[4].trim(), Double.parseDouble(p[5].trim()), Double.parseDouble(p[6].trim()),
            p[7].trim(), Double.parseDouble(p[8].trim()), Double.parseDouble(p[9].trim()),
            LocalTime.parse(p[10].trim()),
            Double.parseDouble(p[11].trim()), Double.parseDouble(p[12].trim()));
        ps.setAvailable(Boolean.parseBoolean(p[13].trim()));
        if (p.length >= 15 && !p[14].isBlank()) {
            ps.setAssignedDriverId(p[14].trim()); ps.setMatched(true);
        }
        return ps;
    }

    public boolean dataExists() { return new File(DRIVERS_FILE).exists(); }
}
