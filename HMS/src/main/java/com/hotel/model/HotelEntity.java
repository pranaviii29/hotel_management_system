package com.hotel.model;

import java.io.Serializable;
public abstract class HotelEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String createdAt;

    public HotelEntity(String id, String createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getCreatedAt() { return createdAt; }

    public abstract String getSummary();

    @Override
    public String toString() {
        return "HotelEntity{id='" + id + "'}";
    }
}
