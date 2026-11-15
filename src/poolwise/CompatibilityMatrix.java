package poolwise;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CompatibilityMatrix {
    private List<Driver> drivers;
    private List<Passenger> passengers;
    private List<CompatibilityScore> scores;

    public CompatibilityMatrix(List<Driver> drivers, List<Passenger> passengers) {
        this.drivers    = drivers;
        this.passengers = passengers;
        this.scores     = new ArrayList<>();
        buildMatrix();
    }

    private void buildMatrix() {
        for (Driver d : drivers) {
            for (Passenger p : passengers) {
                scores.add(new CompatibilityScore(d, p));
            }
        }
        // Sort descending by score
        scores.sort(Comparator.comparingDouble(CompatibilityScore::getScore).reversed());
    }

    public List<CompatibilityScore> getAllScores() { return scores; }

    public List<CompatibilityScore> getScoresForDriver(Driver d) {
        List<CompatibilityScore> result = new ArrayList<>();
        for (CompatibilityScore cs : scores) {
            if (cs.getDriver().getId().equals(d.getId())) result.add(cs);
        }
        result.sort(Comparator.comparingDouble(CompatibilityScore::getScore).reversed());
        return result;
    }

    public List<CompatibilityScore> getScoresForPassenger(Passenger p) {
        List<CompatibilityScore> result = new ArrayList<>();
        for (CompatibilityScore cs : scores) {
            if (cs.getPassenger().getId().equals(p.getId())) result.add(cs);
        }
        result.sort(Comparator.comparingDouble(CompatibilityScore::getScore).reversed());
        return result;
    }

    public void displayMatrix() {
        System.out.println("\n╔═══════ COMPATIBILITY MATRIX ═══════╗");
        System.out.printf("%-15s %-15s %s%n", "DRIVER", "PASSENGER", "SCORE (breakdown)");
        System.out.println("─".repeat(70));
        for (CompatibilityScore cs : scores) {
            System.out.printf("%-15s %-15s %s%n",
                cs.getDriver().getName(),
                cs.getPassenger().getName(),
                cs.toString());
        }
        System.out.println("╚" + "═".repeat(68) + "╝\n");
    }
}
