package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServiceRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;
    private String bookingId;
    private String guestName;
    private String roomNumber;
    private ServiceType serviceType;
    private boolean completed;
    private boolean guestCheckedOut;
    private String requestedAt;
    private String completedAt;

    public ServiceRequest(String bookingId, String guestName, String roomNumber, ServiceType serviceType) {
        this.requestId       = "SR" + System.currentTimeMillis() + (int)(Math.random() * 1000);
        this.bookingId       = bookingId;
        this.guestName       = guestName;
        this.roomNumber      = roomNumber;
        this.serviceType     = serviceType;
        this.completed       = false;
        this.guestCheckedOut = false;
        this.requestedAt     = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    public String getRequestId()        { return requestId; }
    public String getBookingId()        { return bookingId; }
    public String getGuestName()        { return guestName; }
    public String getRoomNumber()       { return roomNumber; }
    public ServiceType getServiceType() { return serviceType; }
    public boolean isCompleted()        { return completed; }
    public boolean isGuestCheckedOut()  { return guestCheckedOut; }
    public String getRequestedAt()      { return requestedAt; }
    public String getCompletedAt()      { return completedAt; }

    public void markCompleted() {
        this.completed   = true;
        this.completedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    public void markGuestCheckedOut() {
        this.guestCheckedOut = true;
    }

    public String getStatus() {
        if (completed)        return "Completed";
        if (guestCheckedOut)  return "Not Completed";
        return "Pending";
    }
}