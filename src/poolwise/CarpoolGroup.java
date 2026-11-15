package poolwise;

import java.util.ArrayList;
import java.util.List;

public class CarpoolGroup {
    private static final double CO2_PER_KM    = 0.21; // kg CO2 per km per car
    private static final double FUEL_PER_KM   = 0.08; // litres per km

    private Driver driver;
    private List<Passenger> passengers;
    private List<Location> routeOrder;
    private double totalExtraDistanceKm;
    private String groupId;

    public CarpoolGroup(String groupId, Driver driver) {
        this.groupId = groupId;
        this.driver = driver;
        this.passengers = new ArrayList<>();
        this.routeOrder = new ArrayList<>();
        this.totalExtraDistanceKm = 0;
    }

    public void addPassenger(Passenger p) { passengers.add(p); }
    public void removePassenger(Passenger p) { passengers.remove(p); }
    public int getPassengerCount() { return passengers.size(); }
    public boolean isAtCapacity() { return passengers.size() >= driver.getAvailableSeats(); }

    public Driver getDriver()              { return driver; }
    public List<Passenger> getPassengers() { return passengers; }
    public List<Location> getRouteOrder()  { return routeOrder; }
    public String getGroupId()             { return groupId; }

    public void setRouteOrder(List<Location> order) { this.routeOrder = order; }
    public void setTotalExtraDistanceKm(double dist) { this.totalExtraDistanceKm = dist; }
    public double getTotalExtraDistanceKm() { return totalExtraDistanceKm; }

    public double getEstimatedCO2SavedKg() {
        // CO2 saved = emissions from N separate cars vs 1 carpooled car
        int carsAvoided = passengers.size(); // each passenger would have used a car
        double baseDistance = driver.getStartLocation().distanceTo(driver.getDestination());
        return carsAvoided * baseDistance * CO2_PER_KM - totalExtraDistanceKm * CO2_PER_KM;
    }

    public double getFuelSavedLiters() {
        int carsAvoided = passengers.size();
        double baseDistance = driver.getStartLocation().distanceTo(driver.getDestination());
        return carsAvoided * baseDistance * FUEL_PER_KM - totalExtraDistanceKm * FUEL_PER_KM;
    }

    public double getTotalRouteDistance() {
        if (routeOrder.size() < 2) return 0;
        double total = 0;
        for (int i = 0; i < routeOrder.size() - 1; i++) {
            total += routeOrder.get(i).distanceTo(routeOrder.get(i + 1));
        }
        return total;
    }

    public void displaySummary() {
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│  CARPOOL GROUP: " + groupId);
        System.out.println("│  Driver:    " + driver.getName() + " (" + driver.getAvailableSeats() + " seats)");
        System.out.println("│  Passengers (" + passengers.size() + "):");
        for (Passenger p : passengers) {
            System.out.println("│    • " + p.getName());
        }
        System.out.println("│  Extra Distance: " + String.format("%.2f", totalExtraDistanceKm) + " km");
        System.out.println("│  CO2 Saved: " + String.format("%.2f", getEstimatedCO2SavedKg()) + " kg");
        System.out.println("│  Fuel Saved: " + String.format("%.2f", getFuelSavedLiters()) + " L");
        if (!routeOrder.isEmpty()) {
            StringBuilder route = new StringBuilder("│  Route: ");
            for (int i = 0; i < routeOrder.size(); i++) {
                route.append(routeOrder.get(i).getName());
                if (i < routeOrder.size() - 1) route.append(" → ");
            }
            System.out.println(route);
        }
        System.out.println("└─────────────────────────────────────────┘");
    }

    @Override
    public String toString() {
        return "CarpoolGroup[" + groupId + "] Driver=" + driver.getName() 
             + " Passengers=" + passengers.size() + "/" + driver.getAvailableSeats()
             + " ExtraKm=" + String.format("%.2f", totalExtraDistanceKm);
    }
}
