package com.hotel.service;

import com.hotel.util.FileHandler;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Multithreading 
// Background auto-save and notification service
public class AutoSaveService {

    // Scheduled thread pool for background tasks
    private final ScheduledExecutorService scheduler;
    private final StringProperty statusMessage;
    private static AutoSaveService instance;

    public static AutoSaveService getInstance() {
        if (instance == null) {
            instance = new AutoSaveService();
        }
        return instance;
    }

    private AutoSaveService() {
        // Daemon thread pool - won't prevent JVM shutdown
        scheduler = Executors.newScheduledThreadPool(2, runnable -> {
            Thread t = new Thread(runnable, "AutoSave-Thread");
            t.setDaemon(true);  // Multithreading - daemon thread
            return t;
        });
        statusMessage = new SimpleStringProperty("System ready");
    }

    /**
     * Starts periodic auto-save every 30 seconds.
     * Demonstrates: Multithreading + Synchronization
     */
    public void startAutoSave() {
        // Scheduled background task - Multithreading
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Background thread performs auto-save
                HotelService service = HotelService.getInstance();
                FileHandler.saveRooms(service.getAllRooms());
                FileHandler.saveBookings(service.getAllBookings());
                FileHandler.appendLog("Auto-save completed");

                // Update UI on JavaFX Application Thread (thread safety)
                Platform.runLater(() ->
                        statusMessage.set("Auto-saved at " +
                                java.time.LocalTime.now().format(
                                        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))));

            } catch (Exception e) {
                System.err.println("Auto-save error: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Sends a simulated notification in background thread.
     */
    public void sendNotification(String message) {
        // Background thread for notifications
        scheduler.schedule(() -> {
            FileHandler.appendLog("Notification: " + message);
            Platform.runLater(() -> statusMessage.set(message));
        }, 0, TimeUnit.SECONDS);
    }

    public StringProperty statusMessageProperty() { return statusMessage; }

    public void shutdown() {
        scheduler.shutdown();
    }
}
