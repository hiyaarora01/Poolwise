package poolwise;

public abstract class User {
    protected String id;
    protected String name;
    protected double reliabilityScore; // 0.0 to 1.0
    protected boolean available;

    public User(String id, String name, double reliabilityScore) {
        this.id = id;
        this.name = name;
        this.reliabilityScore = reliabilityScore;
        this.available = true;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getReliabilityScore() { return reliabilityScore; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setReliabilityScore(double score) { this.reliabilityScore = score; }

    public abstract String getRole();

    @Override
    public String toString() {
        return "[" + getRole() + "] " + name + " (ID: " + id + ", Reliability: " + String.format("%.2f", reliabilityScore) + ")";
    }
}
