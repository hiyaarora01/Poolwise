package com.poolwise.model;

import java.time.LocalDateTime;

/**
 * Abstract base class for all system users.
 * Demonstrates: Abstract class, Encapsulation, Inheritance root.
 */
public abstract class User {
    private String userId;
    private String name;
    private String phone;
    private double reliabilityScore; // 0.0 – 1.0
    private boolean available;
    private LocalDateTime registeredAt;

    public User(String userId, String name, String phone, double reliabilityScore) {
        this.userId          = userId;
        this.name            = name;
        this.phone           = phone;
        this.reliabilityScore = Math.max(0.0, Math.min(1.0, reliabilityScore));
        this.available       = true;
        this.registeredAt    = LocalDateTime.now();
    }

    // Getters
    public String getUserId()             { return userId; }
    public String getName()               { return name; }
    public String getPhone()              { return phone; }
    public double getReliabilityScore()   { return reliabilityScore; }
    public boolean isAvailable()          { return available; }
    public LocalDateTime getRegisteredAt(){ return registeredAt; }

    // Setters
    public void setName(String name)                       { this.name = name; }
    public void setPhone(String phone)                     { this.phone = phone; }
    public void setReliabilityScore(double score)          { this.reliabilityScore = Math.max(0.0, Math.min(1.0, score)); }
    public void setAvailable(boolean available)            { this.available = available; }

    /** Abstract — subclasses declare their role. */
    public abstract String getRole();

    /** Abstract — subclasses return a summary string. */
    public abstract String getSummary();

    @Override
    public String toString() {
        return String.format("[%s] %s (ID: %s, Phone: %s, Reliability: %.2f, Available: %s)",
            getRole(), name, userId, phone, reliabilityScore, available);
    }
}
