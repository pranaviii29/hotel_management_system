package com.hotel.model;

public enum ServiceType {
    ROOM_CLEANING("Room Cleaning", 300.0),
    LAUNDRY("Laundry", 500.0),
    EXTRA_BED("Extra Bed", 800.0),
    MINI_BAR("Mini-bar", 1200.0);

    private final String displayName;
    private final double charge;

    ServiceType(String displayName, double charge) {
        this.displayName = displayName;
        this.charge = charge;
    }

    public String getDisplayName() { return displayName; }
    public double getCharge() { return charge; }

    @Override
    public String toString() { return displayName; }
}
