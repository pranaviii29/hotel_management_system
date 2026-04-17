package com.hotel.model;

import java.util.ArrayList;
import java.util.List;

public class Room extends HotelEntity {
    private static final long serialVersionUID = 1L;

    private String roomNumber;
    private RoomType roomType;          
    private double pricePerNight;
    private boolean available;
    private String floor;
    private int capacity;
    private List<String> amenities;     

    public Room(String roomNumber, RoomType roomType, String floor, int capacity) {
        super("ROOM-" + roomNumber, java.time.LocalDate.now().toString());
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = roomType.getBasePrice();
        this.available = true;
        this.floor = floor;
        this.capacity = capacity;
        this.amenities = new ArrayList<>();
        initDefaultAmenities();
    }

    private void initDefaultAmenities() {
        amenities.add("Wi-Fi");
        amenities.add("Air Conditioning");
        amenities.add("TV");
        switch (roomType) {
            case DELUXE -> { amenities.add("Bathtub"); amenities.add("City View"); }
            case SUITE -> { amenities.add("Jacuzzi"); amenities.add("Living Area"); amenities.add("Sea View"); }
            case PRESIDENTIAL -> { amenities.add("Private Pool"); amenities.add("Butler Service"); amenities.add("Penthouse View"); amenities.add("Kitchenette"); }
            default -> {}
        }
    }

    @Override
    public String getSummary() {
        return String.format("Room %s | %s | Floor %s | Rs %.0f/night | %s",
                roomNumber, roomType.getDisplayName(), floor, pricePerNight,
                available ? "Available" : "Occupied");
    }

    public String getRoomNumber() { return roomNumber; }
    public RoomType getRoomType() { return roomType; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double price) { this.pricePerNight = price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getFloor() { return floor; }
    public int getCapacity() { return capacity; }
    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public String getAmenitiesString() {
        return String.join(", ", amenities);
    }

    public String getStatusDisplay() {
        return available ? "Available" : "Occupied";
    }
}
