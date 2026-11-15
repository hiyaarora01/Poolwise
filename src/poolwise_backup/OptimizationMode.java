package poolwise;

public enum OptimizationMode {
    MIN_CARS("Minimum Cars", "Use fewest possible vehicles"),
    MIN_DISTANCE("Minimum Distance", "Minimize total additional travel distance"),
    MIN_TIME("Minimum Time", "Prioritize fastest arrival for passengers"),
    MIN_CO2("Minimum CO2", "Prioritize environmentally efficient combinations"),
    BALANCED("Balanced Mode", "Balance time, distance, reliability and environmental impact");

    private final String displayName;
    private final String description;

    OptimizationMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return displayName + ": " + description; }
}
