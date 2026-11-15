package poolwise;

import java.util.ArrayList;
import java.util.List;

public class ScenarioSimulator {

    private List<Driver> drivers;
    private List<Passenger> passengers;
    private OptimizationMode mode;

    public ScenarioSimulator(List<Driver> drivers, List<Passenger> passengers, OptimizationMode mode) {
        // Defensive copies
        this.drivers    = new ArrayList<>(drivers);
        this.passengers = new ArrayList<>(passengers);
        this.mode       = mode;
    }

    /** Simulate normal day — all drivers available */
    public CarpoolPlan simulateNormalDay() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║  SCENARIO 1: NORMAL DAY          ║");
        System.out.println("║  All drivers available            ║");
        System.out.println("╚══════════════════════════════════╝");
        setAllDriversAvailable(true);
        return CarpoolOptimizer.optimize(drivers, passengers, mode);
    }

    /** Simulate a driver cancellation */
    public CarpoolPlan simulateDriverCancellation(String driverId) {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║  SCENARIO 2: DRIVER CANCELLED     ║");
        System.out.println("║  Driver ID: " + driverId);
        System.out.println("╚══════════════════════════════════╝");
        setAllDriversAvailable(true);
        for (Driver d : drivers) {
            if (d.getId().equals(driverId)) {
                d.setAvailable(false);
                System.out.println("  ⚠ " + d.getName() + " is now UNAVAILABLE. Recalculating...\n");
            }
        }
        return CarpoolOptimizer.optimize(drivers, passengers, mode);
    }

    /** Simulate reduced detour tolerance */
    public CarpoolPlan simulateReducedDetour(double newMaxDetourKm) {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║  SCENARIO 3: REDUCED MAX DETOUR               ║");
        System.out.println("║  New max detour: " + newMaxDetourKm + " km");
        System.out.println("╚══════════════════════════════════════════════╝");
        setAllDriversAvailable(true);
        for (Driver d : drivers) {
            // Only reduce if current is higher
            if (d.getMaxDetourKm() > newMaxDetourKm) {
                // We need a setter — add it to Driver
                d.setMaxDetourKm(newMaxDetourKm);
            }
        }
        System.out.println("  Recalculating with reduced detour tolerance...\n");
        return CarpoolOptimizer.optimize(drivers, passengers, mode);
    }

    /** Compare all optimization modes */
    public void compareAllModes() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║         COMPARING ALL OPTIMIZATION MODES              ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        setAllDriversAvailable(true);

        for (OptimizationMode m : OptimizationMode.values()) {
            CarpoolPlan plan = CarpoolOptimizer.optimize(new ArrayList<>(drivers), new ArrayList<>(passengers), m);
            System.out.printf("\n[%s]  Groups: %d | Cars Avoided: %d | Extra km: %.2f | CO2 Saved: %.2f kg%n",
                m.getDisplayName(),
                plan.getCarsUsed(),
                plan.getCarsAvoided(),
                plan.getTotalExtraDistance(),
                plan.getTotalCO2Saved());
        }
    }

    private void setAllDriversAvailable(boolean available) {
        for (Driver d : drivers) d.setAvailable(available);
    }
}
