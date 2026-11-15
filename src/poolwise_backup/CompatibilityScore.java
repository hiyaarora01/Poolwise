package poolwise;

import java.time.LocalTime;

public class CompatibilityScore {
    private Driver driver;
    private Passenger passenger;
    private double score;
    private double routeOverlapScore;
    private double pickupDistanceScore;
    private double detourScore;
    private double timeScore;
    private double reliabilityScore;
    private double preferenceScore;

    public CompatibilityScore(Driver driver, Passenger passenger) {
        this.driver = driver;
        this.passenger = passenger;
        calculate();
    }

    private void calculate() {
        this.routeOverlapScore   = calcRouteOverlap();
        this.pickupDistanceScore = calcPickupDistance();
        this.detourScore         = calcDetour();
        this.timeScore           = calcTimeCompatibility();
        this.reliabilityScore    = calcReliability();
        this.preferenceScore     = calcPreferences();

        this.score = routeOverlapScore   * 0.25
                   + pickupDistanceScore * 0.25
                   + detourScore         * 0.20
                   + timeScore           * 0.20
                   + reliabilityScore    * 0.05
                   + preferenceScore     * 0.05;
    }

    private double calcRouteOverlap() {
        // Vector from driver start to destination
        double dLat = driver.getDestination().getLatitude()  - driver.getStartLocation().getLatitude();
        double dLon = driver.getDestination().getLongitude() - driver.getStartLocation().getLongitude();
        // Vector from passenger start to destination
        double pLat = passenger.getDestination().getLatitude()  - passenger.getStartLocation().getLatitude();
        double pLon = passenger.getDestination().getLongitude() - passenger.getStartLocation().getLongitude();

        double dot = dLat * pLat + dLon * pLon;
        double magD = Math.sqrt(dLat * dLat + dLon * dLon);
        double magP = Math.sqrt(pLat * pLat + pLon * pLon);

        if (magD == 0 || magP == 0) return 0.5;

        double cosAngle = dot / (magD * magP);
        cosAngle = Math.max(-1, Math.min(1, cosAngle)); // clamp
        // Convert to 0–1: cosAngle=1 → perfect overlap, cosAngle=-1 → opposite
        return (cosAngle + 1.0) / 2.0;
    }

    private double calcPickupDistance() {
        double pickupDist = driver.getStartLocation().distanceTo(passenger.getStartLocation());
        double maxAcceptable = 5.0; // km
        return Math.max(0, 1.0 - pickupDist / maxAcceptable);
    }

    private double calcDetour() {
        // Extra distance if driver picks up this passenger
        double direct = driver.getStartLocation().distanceTo(driver.getDestination());
        double withPassenger = driver.getStartLocation().distanceTo(passenger.getStartLocation())
                             + passenger.getStartLocation().distanceTo(driver.getDestination());
        double extraDetour = withPassenger - direct;
        if (driver.getMaxDetourKm() <= 0) return 0;
        return Math.max(0, 1.0 - extraDetour / driver.getMaxDetourKm());
    }

    private double calcTimeCompatibility() {
        // Estimate travel time at 60 km/h average
        double distKm = driver.getStartLocation().distanceTo(driver.getDestination());
        double travelMinutes = (distKm / 60.0) * 60.0;
        LocalTime estimated = driver.getDepartureTime().plusMinutes((long) travelMinutes);
        long diffMinutes = java.time.Duration.between(estimated, passenger.getRequiredArrivalTime()).toMinutes();
        // If estimated arrival is before required arrival, good (positive diff = we arrive earlier = good)
        if (diffMinutes >= 0) return 1.0;
        // Late by up to 30 min — partial score
        return Math.max(0, 1.0 + diffMinutes / 30.0);
    }

    private double calcReliability() {
        return (driver.getReliabilityScore() + passenger.getReliabilityScore()) / 2.0;
    }

    private double calcPreferences() {
        // Simple: if no preferences, full score; more preferences = harder to satisfy (penalize slightly)
        int prefCount = passenger.getPreferences().size();
        return Math.max(0, 1.0 - prefCount * 0.1);
    }

    public Driver getDriver()              { return driver; }
    public Passenger getPassenger()        { return passenger; }
    public double getScore()               { return score; }
    public double getRouteOverlapScore()   { return routeOverlapScore; }
    public double getPickupDistanceScore() { return pickupDistanceScore; }
    public double getDetourScore()         { return detourScore; }
    public double getTimeScore()           { return timeScore; }
    public double getReliabilityScore()    { return reliabilityScore; }
    public double getPreferenceScore()     { return preferenceScore; }

    @Override
    public String toString() {
        return String.format(
            "Score: %.3f [Route:%.2f | Pickup:%.2f | Detour:%.2f | Time:%.2f | Reliability:%.2f | Pref:%.2f]",
            score, routeOverlapScore, pickupDistanceScore, detourScore, timeScore, reliabilityScore, preferenceScore
        );
    }
}
