package com.hotel.service;

import com.hotel.model.*;
import com.hotel.util.FileHandler;
import com.hotel.util.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class HotelService {

    private final Repository<Room>           roomRepository;
    private final Repository<Booking>        bookingRepository;
    private final Repository<ServiceRequest> serviceRequestRepository;

    private final Object bookingLock = new Object();

    private static HotelService instance;

    public static synchronized HotelService getInstance() {
        if (instance == null) instance = new HotelService();
        return instance;
    }

    private HotelService() {
        roomRepository           = new Repository<>();
        bookingRepository        = new Repository<>();
        serviceRequestRepository = new Repository<>();
        loadData();
        if (roomRepository.size() == 0) initSampleRooms();
    }

    // ── Room Management ──────────────────────────────────────────────────────

    public void addRoom(Room room) {
        roomRepository.add(room);
        FileHandler.appendLog("Room added: " + room.getRoomNumber());
        saveData();
    }

    public List<Room> getAllRooms()        { return roomRepository.getAll(); }
    public List<Room> getAvailableRooms() { return roomRepository.filter(Room::isAvailable); }

    public List<Room> searchRooms(String query, RoomType typeFilter, boolean availableOnly) {
        return roomRepository.filter(room -> {
            boolean q = query == null || query.isBlank()
                    || room.getRoomNumber().toLowerCase().contains(query.toLowerCase())
                    || room.getRoomType().getDisplayName().toLowerCase().contains(query.toLowerCase())
                    || room.getFloor().toLowerCase().contains(query.toLowerCase());
            boolean t = typeFilter == null || room.getRoomType() == typeFilter;
            boolean a = !availableOnly || room.isAvailable();
            return q && t && a;
        });
    }

    // ── Booking ───────────────────────────────────────────────────────────────

    public synchronized BookingResult bookRoom(Guest guest, Room room,
            LocalDate checkIn, LocalDate checkOut,
            List<ServiceType> services, String specialRequests) {

        synchronized (bookingLock) {
            Optional<Room> fresh = roomRepository.findFirst(
                r -> r.getRoomNumber().equals(room.getRoomNumber()));
            if (fresh.isEmpty() || !fresh.get().isAvailable())
                return new BookingResult(false, null,
                    "Room " + room.getRoomNumber() + " is no longer available.");

            fresh.get().setAvailable(false);

            Booking booking = new Booking(guest, fresh.get(), checkIn, checkOut,
                                          services, specialRequests);
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.add(booking);

            if (services != null) {
                for (ServiceType st : services) {
                    ServiceRequest req = new ServiceRequest(
                        booking.getBookingId(),
                        guest.getName(),
                        fresh.get().getRoomNumber(),
                        st
                    );
                    serviceRequestRepository.add(req);
                    booking.addServiceRequest(req);
                }
            }

            FileHandler.writeRoomStatusRecord(room.getRoomNumber(), false);
            FileHandler.appendLog("Booking confirmed: " + booking.getBookingId()
                + " for " + guest.getName());
            saveData();
            return new BookingResult(true, booking, "Booking confirmed successfully!");
        }
    }

    // ── Checkout ──────────────────────────────────────────────────────────────

    public synchronized CheckoutResult checkOut(String bookingId) {
        synchronized (bookingLock) {
            Optional<Booking> opt = bookingRepository.findFirst(
                b -> b.getBookingId().equals(bookingId));
            if (opt.isEmpty())
                return new CheckoutResult(false, null, "Booking not found.");

            Booking booking = opt.get();

            // Mark all incomplete service requests as guest checked out
            for (ServiceRequest req : booking.getServiceRequests()) {
                if (!req.isCompleted()) {
                    req.markGuestCheckedOut();
                }
            }

            List<ServiceType> refunded = booking.applyRefundsForIncompleteServices();
            booking.setStatus(BookingStatus.CHECKED_OUT);

            Optional<Room> roomOpt = roomRepository.findFirst(
                r -> r.getRoomNumber().equals(booking.getRoom().getRoomNumber()));
            roomOpt.ifPresent(r -> {
                r.setAvailable(true);
                FileHandler.writeRoomStatusRecord(r.getRoomNumber(), true);
            });

            if (!refunded.isEmpty()) {
                StringBuilder refundLog = new StringBuilder("Checkout with REFUND: ")
                    .append(bookingId).append(" | Refunded: ");
                refunded.forEach(st -> refundLog.append(st.getDisplayName()).append(" "));
                refundLog.append("| Refund amount: Rs ").append(
                    String.format("%.2f", booking.getRefundAmount()));
                FileHandler.appendLog(refundLog.toString());
            } else {
                FileHandler.appendLog("Checkout: " + bookingId
                    + " | Room " + booking.getRoom().getRoomNumber() + " now available.");
            }

            saveData();

            String msg = refunded.isEmpty()
                ? "Checkout successful. All services were completed."
                : "Checkout successful with refund of Rs "
                  + String.format("%.2f", booking.getRefundAmount())
                  + " for incomplete services.";

            return new CheckoutResult(true, booking, msg);
        }
    }

    // ── Revenue ───────────────────────────────────────────────────────────────

    public double getTotalRevenue() {
        return bookingRepository
            .filter(b -> b.getStatus() == BookingStatus.CHECKED_OUT)
            .stream()
            .mapToDouble(Booking::getFinalAmountAfterRefund)
            .sum();
    }

    // ── Service Requests ──────────────────────────────────────────────────────

    public ServiceRequest addServiceRequest(Booking booking, ServiceType serviceType) {
        ServiceRequest req = new ServiceRequest(
            booking.getBookingId(), booking.getGuest().getName(),
            booking.getRoom().getRoomNumber(), serviceType
        );
        serviceRequestRepository.add(req);
        booking.addServiceRequest(req);
        FileHandler.appendLog("Service request: " + serviceType.getDisplayName()
            + " for " + booking.getBookingId());
        saveData();
        return req;
    }

    public void markServiceCompleted(ServiceRequest req) {
        req.markCompleted();
        FileHandler.appendLog("Service completed: " + req.getRequestId());
        saveData();
    }

    public List<ServiceRequest> getAllServiceRequests() {
        return serviceRequestRepository.getAll();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Booking> getAllBookings()     { return bookingRepository.getAll(); }
    public List<Booking> getActiveBookings() {
        return bookingRepository.filter(b ->
            b.getStatus() == BookingStatus.CONFIRMED
            || b.getStatus() == BookingStatus.CHECKED_IN);
    }
    public List<Booking> getBookingHistory() {
        return bookingRepository.filter(b ->
            b.getStatus() == BookingStatus.CHECKED_OUT
            || b.getStatus() == BookingStatus.CANCELLED);
    }
    public Optional<Booking> findBookingById(String id) {
        return bookingRepository.findFirst(b -> b.getBookingId().equals(id));
    }

    // ── Statistics ────────────────────────────────────────────────────────────

    public int getTotalRooms()          { return roomRepository.size(); }
    public int getAvailableRoomsCount() { return getAvailableRooms().size(); }
    public int getOccupiedRoomsCount()  { return getTotalRooms() - getAvailableRoomsCount(); }
    public int getActiveBookingsCount() { return getActiveBookings().size(); }

    // ── Persistence ───────────────────────────────────────────────────────────

    private void saveData() {
        FileHandler.saveRooms(roomRepository.getAll());
        FileHandler.saveBookings(bookingRepository.getAll());
    }

    private void loadData() {
        FileHandler.loadRooms().forEach(roomRepository::add);
        FileHandler.loadBookings().forEach(b -> {
            bookingRepository.add(b);
            b.getServiceRequests().forEach(serviceRequestRepository::add);
        });
        reconcileRoomAvailability();
    }

    private void reconcileRoomAvailability() {
        Set<String> activeRoomNumbers = bookingRepository
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                      || b.getStatus() == BookingStatus.CHECKED_IN)
            .stream()
            .map(b -> b.getRoom().getRoomNumber())
            .collect(Collectors.toSet());

        boolean changed = false;
        for (Room room : roomRepository.getAll()) {
            if (!room.isAvailable() && !activeRoomNumbers.contains(room.getRoomNumber())) {
                room.setAvailable(true);
                changed = true;
            }
        }
        if (changed) saveData();
    }

    private void initSampleRooms() {
        addRoom(new Room("101", RoomType.STANDARD,     "1", 2));
        addRoom(new Room("102", RoomType.STANDARD,     "1", 2));
        addRoom(new Room("103", RoomType.STANDARD,     "1", 2));
        addRoom(new Room("201", RoomType.DELUXE,       "2", 2));
        addRoom(new Room("202", RoomType.DELUXE,       "2", 2));
        addRoom(new Room("203", RoomType.DELUXE,       "2", 3));
        addRoom(new Room("301", RoomType.SUITE,        "3", 3));
        addRoom(new Room("302", RoomType.SUITE,        "3", 4));
        addRoom(new Room("401", RoomType.PRESIDENTIAL, "4", 4));
        addRoom(new Room("402", RoomType.PRESIDENTIAL, "4", 5));
    }

    public static class BookingResult {
        public final boolean success;
        public final Booking booking;
        public final String  message;
        public BookingResult(boolean s, Booking b, String m) {
            success = s; booking = b; message = m;
        }
    }

    public static class CheckoutResult {
        public final boolean success;
        public final Booking booking;
        public final String  message;
        public CheckoutResult(boolean s, Booking b, String m) {
            success = s; booking = b; message = m;
        }
        public boolean hasRefund() {
            return booking != null && booking.getRefundAmount() > 0;
        }
    }
}