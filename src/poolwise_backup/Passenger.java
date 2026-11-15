package poolwise;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Passenger extends User {
    private Location startLocation;
    private Location destination;
    private LocalTime requiredArrivalTime;
    private double maxWaitMinutes;
    private List<String> preferences;

    public Passenger(String id, String name, double reliabilityScore,
                     Location startLocation, Location destination,
                     LocalTime requiredArrivalTime, double maxWaitMinutes) {
        super(id, name, reliabilityScore);
        this.startLocation = startLocation;
        this.destination = destination;
        this.requiredArrivalTime = requiredArrivalTime;
        this.maxWaitMinutes = maxWaitMinutes;
        this.preferences = new ArrayList<>();
    }

    public Location getStartLocation() { return startLocation; }
    public Location getDestination() { return destination; }
    public LocalTime getRequiredArrivalTime() { return requiredArrivalTime; }
    public double getMaxWaitMinutes() { return maxWaitMinutes; }
    public List<String> getPreferences() { return preferences; }
    public void addPreference(String pref) { preferences.add(pref); }

    @Override
    public String getRole() { return "PASSENGER"; }

    @Override
    public String toString() {
        return super.toString() + "\n  Route: " + startLocation.getName() + " → " + destination.getName()
             + "\n  Must arrive by: " + requiredArrivalTime + ", Max wait: " + maxWaitMinutes + " min"
             + (preferences.isEmpty() ? "" : "\n  Preferences: " + preferences);
    }
}
