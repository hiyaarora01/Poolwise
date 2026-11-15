package poolwise;

import java.time.LocalTime;

public class Driver extends User {
    private Location startLocation;
    private Location destination;
    private LocalTime departureTime;
    private int availableSeats;
    private double maxDetourKm;

    public Driver(String id, String name, double reliabilityScore,
                  Location startLocation, Location destination,
                  LocalTime departureTime, int availableSeats, double maxDetourKm) {
        super(id, name, reliabilityScore);
        this.startLocation = startLocation;
        this.destination = destination;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
        this.maxDetourKm = maxDetourKm;
    }

    public Location getStartLocation() { return startLocation; }
    public Location getDestination() { return destination; }
    public LocalTime getDepartureTime() { return departureTime; }
    public int getAvailableSeats() { return availableSeats; }
    public double getMaxDetourKm() { return maxDetourKm; }
    public void setAvailableSeats(int seats) { this.availableSeats = seats; }
    public void setMaxDetourKm(double km) { this.maxDetourKm = km; }

    public double getDirectDistance() {
        return startLocation.distanceTo(destination);
    }

    @Override
    public String getRole() { return "DRIVER"; }

    @Override
    public String toString() {
        return super.toString() + "\n  Route: " + startLocation.getName() + " → " + destination.getName()
             + "\n  Departs: " + departureTime + ", Seats: " + availableSeats + ", Max Detour: " + maxDetourKm + " km";
    }
}
