package poolwise;

import java.util.ArrayList;
import java.util.List;

public class CarpoolPlan {
    private List<CarpoolGroup> groups;
    private String optimizationMode;
    private List<Passenger> unassignedPassengers;
    private int totalPassengersInput;
    private int totalDriversInput;

    public CarpoolPlan(String optimizationMode, int totalPassengersInput, int totalDriversInput) {
        this.optimizationMode      = optimizationMode;
        this.groups                = new ArrayList<>();
        this.unassignedPassengers  = new ArrayList<>();
        this.totalPassengersInput  = totalPassengersInput;
        this.totalDriversInput     = totalDriversInput;
    }

    public void addGroup(CarpoolGroup group) { groups.add(group); }
    public void addUnassignedPassenger(Passenger p) { unassignedPassengers.add(p); }
    public List<CarpoolGroup> getGroups() { return groups; }
    public List<Passenger> getUnassignedPassengers() { return unassignedPassengers; }

    public int getCarsUsed()     { return groups.size(); }
    public int getCarsAvoided()  { 
        int assigned = groups.stream().mapToInt(CarpoolGroup::getPassengerCount).sum();
        return assigned; // each assigned passenger would have been a separate car
    }

    public double getTotalExtraDistance() {
        return groups.stream().mapToDouble(CarpoolGroup::getTotalExtraDistanceKm).sum();
    }

    public double getTotalCO2Saved() {
        return groups.stream().mapToDouble(CarpoolGroup::getEstimatedCO2SavedKg).sum();
    }

    public double getTotalFuelSaved() {
        return groups.stream().mapToDouble(CarpoolGroup::getFuelSavedLiters).sum();
    }

    public int getTotalPassengersServed() {
        return groups.stream().mapToInt(CarpoolGroup::getPassengerCount).sum();
    }

    public void displayFullPlan() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║           POOLWISE — CARPOOL PLAN                ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  Optimization Mode: " + optimizationMode);
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        for (CarpoolGroup g : groups) {
            g.displaySummary();
            System.out.println();
        }

        if (!unassignedPassengers.isEmpty()) {
            System.out.println("⚠ Unassigned Passengers:");
            for (Passenger p : unassignedPassengers) {
                System.out.println("  • " + p.getName() + " (no compatible driver found)");
            }
            System.out.println();
        }

        System.out.println("════════════ ENVIRONMENTAL IMPACT SUMMARY ════════════");
        System.out.println("  Optimization Mode    : " + optimizationMode);
        System.out.println("  Total Drivers        : " + totalDriversInput);
        System.out.println("  Total Passengers     : " + totalPassengersInput);
        System.out.println("  Carpool Groups Formed: " + getCarsUsed());
        System.out.println("  Passengers Served    : " + getTotalPassengersServed());
        System.out.println("  Cars Avoided         : " + getCarsAvoided());
        System.out.println("  Total Extra Distance : " + String.format("%.2f", getTotalExtraDistance()) + " km");
        System.out.println("  Estimated CO2 Saved  : " + String.format("%.2f", getTotalCO2Saved()) + " kg");
        System.out.println("  Estimated Fuel Saved : " + String.format("%.2f", getTotalFuelSaved()) + " L");
        System.out.println("═══════════════════════════════════════════════════════");
    }
}
