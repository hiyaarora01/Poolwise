package com.poolwise.model;

/**
 * Vehicle model — aggregated by Driver.
 * Demonstrates: Aggregation relationship.
 */
public class Vehicle {
    private String registrationNumber;
    private String make;   // e.g. "Honda"
    private String model;  // e.g. "City"
    private String color;
    private int    capacity; // total passenger capacity (not counting driver)

    public Vehicle(String registrationNumber, String make, String model, String color, int capacity) {
        this.registrationNumber = registrationNumber;
        this.make               = make;
        this.model              = model;
        this.color              = color;
        this.capacity           = capacity;
    }

    public String getRegistrationNumber() { return registrationNumber; }
    public String getMake()               { return make; }
    public String getModel()              { return model; }
    public String getColor()              { return color; }
    public int    getCapacity()           { return capacity; }

    public void setRegistrationNumber(String r) { this.registrationNumber = r; }
    public void setMake(String m)               { this.make = m; }
    public void setModel(String m)              { this.model = m; }
    public void setColor(String c)              { this.color = c; }
    public void setCapacity(int c)              { this.capacity = c; }

    public String toCsv() { return String.join(",", registrationNumber, make, model, color, String.valueOf(capacity)); }

    @Override
    public String toString() {
        return String.format("%s %s (%s) — %s [Cap: %d]", make, model, color, registrationNumber, capacity);
    }
}
