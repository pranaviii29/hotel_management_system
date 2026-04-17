package com.hotel.model;

public enum RoomType {
    STANDARD("Standard", 2500.0),
    DELUXE("Deluxe", 4500.0),
    SUITE("Suite", 8000.0),
    PRESIDENTIAL("Presidential Suite", 15000.0);

    private final String displayName;
    private final double basePrice;

    RoomType(String displayName, double basePrice) {
        this.displayName = displayName;
        this.basePrice = basePrice;
    }

    public String getDisplayName() { return displayName; }
    public double getBasePrice() { return basePrice; }

    @Override
    public String toString() { return displayName; }
}
