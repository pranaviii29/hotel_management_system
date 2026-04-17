\# Hotel Management System



<div align="center">

A fully functional, desktop-based Hotel Management System built with Java 17 and JavaFX 21.

Designed as a comprehensive lab project demonstrating every core Java concept—OOP, multithreading, I/O streams, generics, serialization, and more,wrapped in a clean, professional GUI.



</div>





\## Overview



\*\*Silverview Grand HMS\*\* is a standalone JavaFX desktop application that simulates a real hotel management workflow. It covers the full guest lifecycle,from room setup and booking, through active stay management, to checkout and billing,all without a database, relying entirely on Java I/O and serialization for persistence.



> \*\*No database required.\*\* All data is stored locally using Java serialization (`.dat` files), character stream logs, and random access files — stored in `\~/SilverviewHMS/` on the user's machine.



\---







\---

\## Project Structure



```

HMS/

├── pom.xml                                         # Maven build — Java 17, JavaFX 21

└── src/

&#x20;   └── main/

&#x20;       ├── resources/

&#x20;       └── java/

&#x20;           ├── module-info.java                    # JPMS module descriptor

&#x20;           └── com/hotel/

&#x20;               │

&#x20;               ├── MainApp.java                    # JavaFX entry point, sidebar, navigation

&#x20;               │

&#x20;               ├── model/                          # Data models (all Serializable)

&#x20;               │   ├── HotelEntity.java            #   Abstract base class

&#x20;               │   ├── Room.java                   #   Room details + amenities

&#x20;               │   ├── Guest.java                  #   Guest with wrapper class (Integer age)

&#x20;               │   ├── Booking.java                #   Booking with billing logic

&#x20;               │   ├── ServiceRequest.java         #   Individual service request

&#x20;               │   ├── RoomType.java               #   Enum: STANDARD / DELUXE / SUITE / PRESIDENTIAL

&#x20;               │   ├── BookingStatus.java           #   Enum: CONFIRMED / CHECKED\_OUT / etc.

&#x20;               │   └── ServiceType.java            #   Enum: CLEANING / LAUNDRY / BED / MINIBAR

&#x20;               │

&#x20;               ├── service/                        # Business logic

&#x20;               │   ├── HotelService.java           #   Core service — synchronized booking, singleton

&#x20;               │   └── AutoSaveService.java        #   Background threads — auto-save, notifications

&#x20;               │

&#x20;               ├── ui/                             # JavaFX panels (one per screen)

&#x20;               │   ├── UIStyles.java               #   Centralized CSS-in-Java constants

&#x20;               │   ├── DashboardPanel.java         #   Live stats + hotel info

&#x20;               │   ├── RoomsPanel.java             #   Add / view / filter rooms

&#x20;               │   ├── BookingPanel.java           #   New booking form + live bill preview

&#x20;               │   ├── ActiveBookingsPanel.java    #   Confirmed bookings + services booked

&#x20;               │   ├── CheckoutPanel.java          #   Checkout flow + history

&#x20;               │   └── ServiceRequestsPanel.java   #   All requests with Mark Completed

&#x20;               │

&#x20;               └── util/                           # Utility / infrastructure

&#x20;                   ├── Repository.java             #   Generic<T> in-memory repository

&#x20;                   ├── FileHandler.java            #   All I/O: streams, RAF, serialization

&#x20;                   └── ReceiptGenerator.java       #   Formatted receipt via character streams

```



\---



\## Architecture



This project follows a layered \*\*MVC-inspired\*\* architecture:



```

┌─────────────────────────────────────────────────────┐

│                   JavaFX UI Layer                   │

│  DashboardPanel │ BookingPanel │ RoomsPanel │ ...   │

│         MainApp.java  (navigation controller)       │

└────────────────────────┬────────────────────────────┘

&#x20;                        │ calls

┌────────────────────────▼────────────────────────────┐

│                  Service Layer                      │

│   HotelService (singleton, synchronized)            │

│   AutoSaveService (ScheduledExecutorService)        │

└────────────┬───────────────────────┬────────────────┘

&#x20;            │ reads/writes          │ persistence

┌────────────▼────────┐   ┌──────────▼──────────────┐

│    Model Layer      │   │     Utility Layer        │

│  Room, Guest,       │   │  FileHandler (I/O)       │

│  Booking, etc.      │   │  Repository<T> (Generics)│

│  (Serializable)     │   │  ReceiptGenerator        │

└─────────────────────┘   └──────────────────────────┘

```



\*\*Key design decisions:\*\*

\- \*\*Singleton\*\* `HotelService` — single source of truth for all data, shared across all panels

\- \*\*Generic `Repository<T>`\*\* — reusable data store, no code duplication per model type

\- \*\*`synchronized` + lock object\*\* — prevents race conditions in concurrent booking scenarios

\- \*\*Daemon threads\*\* — auto-save runs in background and never blocks JVM shutdown

\- \*\*`Platform.runLater()`\*\* — all background thread UI updates routed through JavaFX Application Thread



\---



\## Getting Started



\### Prerequisites



| Requirement | Version |

|---|---|

| JDK | 17 or higher |

| Maven | 3.6+ |

| JavaFX | Managed by Maven (no manual install needed) |



\### Clone \& Run



```bash

\# Clone the repository

git clone https://github.com/pranaviii29/silverview-hms.git

cd silverview-hms/HMS



\# Build and run

mvn clean javafx:run

```



That's it. Maven downloads JavaFX automatically on first run.



\### Build a JAR (optional)



```bash

mvn clean package

```





\## Tech Stack



| Technology | Purpose |

|---|---|

| \*\*Java 17\*\* | Core language (LTS release) |

| \*\*JavaFX 21.0.2\*\* | Desktop GUI framework |

| \*\*Maven 3\*\* | Build system + dependency management |

| \*\*JPMS (Java Module System)\*\* | `module-info.java` for encapsulated module |

| \*\*Java Serialization\*\* | Persistent data storage (no database) |

| \*\*`java.util.concurrent`\*\* | Thread pool for background auto-save |



\---

