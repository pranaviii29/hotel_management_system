package com.hotel.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// Inheritance - Booking extends HotelEntity
public class Booking extends HotelEntity {
    private static final long serialVersionUID = 2L;

    private String bookingId;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus status;               // Enum usage
    private List<ServiceType> services;         // Generics + Collection
    private List<ServiceRequest> serviceRequests;
    private String specialRequests;

    // ── Original billed amounts (calculated at booking time) ──────────────────
    private double totalAmount;
    private double gstAmount;
    private double serviceAmount;

    // ── Refund fields (populated at checkout if services incomplete) ──────────
    private double refundAmount;                        // total refunded
    private double finalAmountAfterRefund;              // what hotel actually earns
    private List<ServiceType> refundedServices;         // which services were refunded

    private static final double GST_RATE = 0.18;

    public Booking(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut,
                   List<ServiceType> services, String specialRequests) {
        super("BKG-" + System.currentTimeMillis(), LocalDate.now().toString());
        this.bookingId         = "BKG-" + System.currentTimeMillis();
        this.guest             = guest;
        this.room              = room;
        this.checkInDate       = checkIn;
        this.checkOutDate      = checkOut;
        this.status            = BookingStatus.CONFIRMED;
        this.services          = services != null ? new ArrayList<>(services) : new ArrayList<>();
        this.serviceRequests   = new ArrayList<>();
        this.specialRequests   = specialRequests;
        this.refundedServices  = new ArrayList<>();
        this.refundAmount      = 0.0;
        calculateAmount();
        this.finalAmountAfterRefund = this.totalAmount; // initially same as total
    }

    // ── Billing calculation at booking time ────────────────────────────────────
    private void calculateAmount() {
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nights <= 0) nights = 1;

        double roomCharge = room.getPricePerNight() * nights;

        // Wrapper class autoboxing in calculation
        Double serviceCharge = 0.0;
        for (ServiceType s : services) {
            serviceCharge += s.getCharge();  // Autoboxing
        }

        this.serviceAmount = serviceCharge;
        this.gstAmount     = (roomCharge + serviceCharge) * GST_RATE;
        this.totalAmount   = roomCharge + serviceCharge + gstAmount;
    }

    /**
     * Called at checkout time.
     * Scans all service requests — any that are still PENDING (not completed)
     * get refunded. The refund covers the service charge + the GST portion
     * that was applied to that service.
     *
     * @return list of ServiceType entries that were refunded
     */
    public List<ServiceType> applyRefundsForIncompleteServices() {
        refundedServices = new ArrayList<>();

        for (ServiceRequest req : serviceRequests) {
            if (!req.isCompleted()) {
                refundedServices.add(req.getServiceType());
            }
        }

        if (refundedServices.isEmpty()) {
            refundAmount            = 0.0;
            finalAmountAfterRefund  = totalAmount;
            return refundedServices;
        }

        // Calculate refund: service charge + the GST that was charged on that service
        double rawRefund = 0.0;
        for (ServiceType st : refundedServices) {
            rawRefund += st.getCharge();
        }
        // GST was applied on (roomCharge + totalServiceCharge) as a whole.
        // Proportionally refund GST for the incomplete portion.
        double gstOnRefundedServices = rawRefund * GST_RATE;
        refundAmount           = rawRefund + gstOnRefundedServices;
        finalAmountAfterRefund = totalAmount - refundAmount;

        return refundedServices;
    }

    // Polymorphism 
    @Override
    public String getSummary() {
        return String.format("Booking %s | Room %s | %s | %s to %s | Rs %.2f",
                bookingId, room.getRoomNumber(), guest.getName(),
                checkInDate, checkOutDate, finalAmountAfterRefund);
    }

    public long getNights() {
        long n = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        return n <= 0 ? 1 : n;
    }

    public double getRoomChargeOnly() {
        return room.getPricePerNight() * getNights();
    }

    //Getters
    public String getBookingId()                    { return bookingId; }
    public Guest getGuest()                         { return guest; }
    public Room getRoom()                           { return room; }
    public LocalDate getCheckInDate()               { return checkInDate; }
    public LocalDate getCheckOutDate()              { return checkOutDate; }
    public BookingStatus getStatus()                { return status; }
    public void setStatus(BookingStatus s)          { this.status = s; }
    public List<ServiceType> getServices()          { return services; }
    public List<ServiceRequest> getServiceRequests(){ return serviceRequests; }
    public void addServiceRequest(ServiceRequest r) { serviceRequests.add(r); }
    public String getSpecialRequests()              { return specialRequests; }
    public double getTotalAmount()                  { return totalAmount; }
    public double getFinalAmountAfterRefund()       { return finalAmountAfterRefund; }
    public double getRefundAmount()                 { return refundAmount; }
    public List<ServiceType> getRefundedServices()  { return refundedServices; }
    public double getGstAmount()                    { return gstAmount; }
    public double getServiceAmount()                { return serviceAmount; }
}
