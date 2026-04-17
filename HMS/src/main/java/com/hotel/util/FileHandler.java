package com.hotel.util;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.model.ServiceRequest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// I/O Streams + Serialization/Deserialization - mandatory Java concept
public class FileHandler {

    private static final String DATA_DIR = System.getProperty("user.home") + "/SilverviewHMS/";
    private static final String ROOMS_FILE = DATA_DIR + "rooms.dat";
    private static final String BOOKINGS_FILE = DATA_DIR + "bookings.dat";
    private static final String LOG_FILE = DATA_DIR + "activity.log";

    static {
        // Create data directory if it doesn't exist
        new File(DATA_DIR).mkdirs();
    }

    // ─── Serialization (Object Output - Byte Stream) ───────────────────────
    @SuppressWarnings("unchecked")
    public static void saveRooms(List<Room> rooms) {
        // Byte stream serialization
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(ROOMS_FILE)))) {
            oos.writeObject(rooms);
        } catch (IOException e) {
            System.err.println("Error saving rooms: " + e.getMessage());
        }
    }

    // ─── Deserialization ───────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public static List<Room> loadRooms() {
        File f = new File(ROOMS_FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(ROOMS_FILE)))) {
            return (List<Room>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading rooms: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    public static void saveBookings(List<Booking> bookings) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(BOOKINGS_FILE)))) {
            oos.writeObject(bookings);
        } catch (IOException e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Booking> loadBookings() {
        File f = new File(BOOKINGS_FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(BOOKINGS_FILE)))) {
            return (List<Booking>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ─── Character Stream - Activity Log ──────────────────────────────────
    public static void appendLog(String message) {
        // Character stream (Writer) for text-based logging
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(LOG_FILE, true))) {  // append = true
            writer.write(java.time.LocalDateTime.now() + " | " + message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing log: " + e.getMessage());
        }
    }

    public static List<String> readLog() {
        List<String> lines = new ArrayList<>();
        File f = new File(LOG_FILE);
        if (!f.exists()) return lines;
        // Character stream (Reader) for reading text log
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading log: " + e.getMessage());
        }
        return lines;
    }

    // ─── Random Access File - update room availability in-place ───────────
    public static void writeRoomStatusRecord(String roomNumber, boolean available) {
        // Random Access File for efficient targeted updates
        String raFilePath = DATA_DIR + "room_status.raf";
        try (RandomAccessFile raf = new RandomAccessFile(raFilePath, "rw")) {
            // Format: each record is 32 bytes: 20 bytes roomNumber + 1 byte status + 11 padding
            long fileLen = raf.length();
            long recordSize = 32;
            boolean found = false;

            for (long pos = 0; pos < fileLen; pos += recordSize) {
                raf.seek(pos);
                byte[] roomBytes = new byte[20];
                raf.read(roomBytes);
                String storedRoom = new String(roomBytes).trim();
                if (storedRoom.equals(roomNumber)) {
                    raf.write(available ? 1 : 0);
                    found = true;
                    break;
                }
            }

            if (!found) {
                raf.seek(fileLen);
                byte[] roomBytes = new byte[20];
                byte[] rn = roomNumber.getBytes();
                System.arraycopy(rn, 0, roomBytes, 0, Math.min(rn.length, 20));
                raf.write(roomBytes);
                raf.write(available ? 1 : 0);
                byte[] padding = new byte[11];
                raf.write(padding);
            }
        } catch (IOException e) {
            System.err.println("RAF error: " + e.getMessage());
        }
    }

    public static String getDataDir() { return DATA_DIR; }
}
