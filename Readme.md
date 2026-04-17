<div align="center">

# 🏨Hotel Management System

A JavaFX-based Hotel Management System for managing rooms, bookings, services, and checkouts.

</div>

---

## Overview

This is a standalone JavaFX desktop application that simulates a real hotel management workflow. It covers the full guest lifecycle — from room setup and booking, through active stay management, to checkout and billing — all without a database, relying entirely on Java I/O and serialization for persistence.

> **No database required.** All data is stored locally using Java serialization (`.dat` files), character stream logs, and random access files — stored in `~/SilverviewHMS/` on the user's machine.

---

## Project Structure

```
HMS/
├── pom.xml
└── src/
    └── main/
        ├── resources/
        └── java/
            ├── module-info.java
            └── com/hotel/
                ├── MainApp.java
                ├── model/
                │   ├── HotelEntity.java
                │   ├── Room.java
                │   ├── Guest.java
                │   ├── Booking.java
                │   ├── ServiceRequest.java
                │   ├── RoomType.java
                │   ├── BookingStatus.java
                │   └── ServiceType.java
                ├── service/
                │   ├── HotelService.java
                │   └── AutoSaveService.java
                ├── ui/
                │   ├── UIStyles.java
                │   ├── DashboardPanel.java
                │   ├── RoomsPanel.java
                │   ├── BookingPanel.java
                │   ├── ActiveBookingsPanel.java
                │   ├── CheckoutPanel.java
                │   └── ServiceRequestsPanel.java
                └── util/
                    ├── Repository.java
                    ├── FileHandler.java
                    └── ReceiptGenerator.java
```

---

## Architecture

This project follows a layered **MVC-inspired** architecture:

```
┌─────────────────────────────────────────────────────┐
│                   JavaFX UI Layer                   │
│  DashboardPanel │ BookingPanel │ RoomsPanel │ ...   │
│         MainApp.java  (navigation controller)       │
└────────────────────────┬────────────────────────────┘
                         │ calls
┌────────────────────────▼────────────────────────────┐
│                  Service Layer                      │
│   HotelService (singleton, synchronized)            │
│   AutoSaveService (ScheduledExecutorService)        │
└────────────┬───────────────────────┬────────────────┘
             │ reads/writes          │ persistence
┌────────────▼────────┐   ┌──────────▼──────────────┐
│    Model Layer      │   │     Utility Layer        │
│  Room, Guest,       │   │  FileHandler (I/O)       │
│  Booking, etc.      │   │  Repository<T> (Generics)│
│  (Serializable)     │   │  ReceiptGenerator        │
└─────────────────────┘   └──────────────────────────┘
```

**Key design decisions:**
- **Singleton** `HotelService` — single source of truth for all data, shared across all panels
- **Generic `Repository<T>`** — reusable data store, no code duplication per model type
- **`synchronized` + lock object** — prevents race conditions in concurrent booking scenarios
- **Daemon threads** — auto-save runs in background and never blocks JVM shutdown
- **`Platform.runLater()`** — all background thread UI updates routed through JavaFX Application Thread

---

## Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| JDK | 21 or higher |
| Maven | 3.6+ |
| JavaFX | Managed by Maven (no manual install needed) |

### Clone & Run

```bash
# Clone the repository
git clone https://github.com/pranaviii29/hotel_management_system.git
cd hotel_management_system/HMS

# Build and run
mvn clean javafx:run
```

Maven downloads JavaFX automatically on first run.

### Build a JAR (optional)

```bash
mvn clean package
```

---

## Tech Stack

| Technology | Purpose |
|---|---|
| **Java 21** | Core language (LTS release) |
| **JavaFX 21.0.2** | Desktop GUI framework |
| **Maven 3** | Build system + dependency management |
| **JPMS** | `module-info.java` for encapsulated module |
| **Java Serialization** | Persistent data storage (no database) |
| **`java.util.concurrent`** | Thread pool for background auto-save |

---
