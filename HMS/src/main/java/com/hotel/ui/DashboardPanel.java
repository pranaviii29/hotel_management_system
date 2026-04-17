package com.hotel.ui;

import com.hotel.service.HotelService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

public class DashboardPanel extends ScrollPane {

    private final HotelService svc;
    private Label totalRoomsVal, availableVal, occupiedVal, bookingsVal, revenueVal;

    public DashboardPanel() {
        this.svc = HotelService.getInstance();
        setFitToWidth(true);
        setStyle("-fx-background: #F2F3F5; -fx-background-color: #F2F3F5; -fx-border-color: transparent;");
        setContent(buildContent());
    }

    private VBox buildContent() {
        VBox root = new VBox(26);
        root.setPadding(new Insets(30, 36, 36, 36));
        root.setStyle("-fx-background-color: #F2F3F5;");

        VBox hdr = new VBox(3);
        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1C2833;");
        Label sub = new Label("Overview of Silverview Grand Hotel operations");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #717D7E;");
        hdr.getChildren().addAll(title, sub);

        totalRoomsVal = new Label("0");
        availableVal  = new Label("0");
        occupiedVal   = new Label("0");
        bookingsVal   = new Label("0");
        revenueVal    = new Label("Rs 0");

        HBox statsRow = new HBox(14);
        statsRow.getChildren().addAll(
            statCard("TOTAL ROOMS",     totalRoomsVal, "#2C3E50"),
            statCard("AVAILABLE",       availableVal,  "#1E8449"),
            statCard("OCCUPIED",        occupiedVal,   "#922B21"),
            statCard("ACTIVE BOOKINGS", bookingsVal,   "#1A5276"),
            statCard("REVENUE",         revenueVal,    "#6C3483")
        );

        VBox infoCard = buildCard("Hotel Information",
            infoRow("Hotel Name",     "Silverview Grand Hotel",              false),
            infoRow("Location",       "123 Marine Drive, Udupi, Karnataka",  true),
            infoRow("Contact",        "+91-824-2200000",                     false),
            infoRow("Email",          "reservations@silverviewgrand.com",    true),
            infoRow("Check-In Time",  "12:00 PM",                            false),
            infoRow("Check-Out Time", "11:00 AM",                            true),
            infoRow("GST Rate",       "18%  (on room charge + services)",    false)
        );

        Label rtHdr = sh("Room Types & Pricing");
        HBox rtRow  = new HBox(14);
        rtRow.getChildren().addAll(
            rtCard("Standard",           "Rs 2,500",  "Up to 2 guests", "#EAF0F6", "#2C3E50"),
            rtCard("Deluxe",             "Rs 4,500",  "Up to 3 guests", "#D6EAF8", "#1A5276"),
            rtCard("Suite",              "Rs 8,000",  "Up to 4 guests", "#D5F5E3", "#1E8449"),
            rtCard("Presidential Suite", "Rs 15,000", "Up to 5 guests", "#E8DAEF", "#6C3483")
        );

        Label svcHdr = sh("Additional Services");
        HBox svcRow  = new HBox(14);
        svcRow.getChildren().addAll(
            svcCard("Room Cleaning", "Rs 300"),
            svcCard("Laundry",       "Rs 500"),
            svcCard("Extra Bed",     "Rs 800"),
            svcCard("Mini-bar",      "Rs 1,200")
        );

        Label guideHdr = sh("Navigation Guide");
        VBox guideCard = buildCard("",
            infoRow("Room Management",    "Add rooms and view their availability",              false),
            infoRow("New Booking",        "Reserve a room for a guest with services",           true),
            infoRow("Active Bookings",    "View confirmed bookings, request in-room services",  false),
            infoRow("Checkout & History", "Process guest checkouts and browse history",         true),
            infoRow("Service Requests",   "Track all service requests across bookings",         false)
        );

        root.getChildren().addAll(hdr, statsRow, sh("Hotel Information"), infoCard,
                rtHdr, rtRow, svcHdr, svcRow, guideHdr, guideCard);
        refresh();
        return root;
    }

    private VBox statCard(String tag, Label val, String color) {
        VBox c = new VBox(6);
        c.setPadding(new Insets(18));
        c.setStyle("-fx-background-color: white;-fx-border-color: #D5D8DC;-fx-border-radius:5;-fx-background-radius:5;" +
                   "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.07),6,0,0,2);");
        HBox.setHgrow(c, Priority.ALWAYS);
        Label t = new Label(tag);
        t.setStyle("-fx-font-size:10px;-fx-text-fill:#717D7E;-fx-font-weight:bold;");
        val.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        Region bar = new Region();
        bar.setPrefHeight(3); bar.setMaxWidth(32);
        bar.setStyle("-fx-background-color:" + color + ";-fx-background-radius:2;");
        c.getChildren().addAll(t, val, bar);
        return c;
    }

    private VBox buildCard(String heading, HBox... rows) {
        VBox card = new VBox(0);
        card.setStyle(UIStyles.CARD);
        if (!heading.isEmpty()) {
            Label h = new Label(heading);
            h.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1C2833;-fx-padding:12 16 10 16;");
            card.getChildren().add(h);
        }
        card.getChildren().addAll(rows);
        return card;
    }

    private HBox infoRow(String key, String value, boolean shade) {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 18, 10, 18));
        row.setStyle(shade ? "-fx-background-color:#F8F9FA;" : "-fx-background-color:white;");
        Label k = new Label(key);
        k.setStyle("-fx-font-size:12px;-fx-text-fill:#717D7E;-fx-min-width:170;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size:12px;-fx-text-fill:#1C2833;-fx-font-weight:bold;");
        row.getChildren().addAll(k, v);
        return row;
    }

    private VBox rtCard(String name, String price, String cap, String bg, String color) {
        VBox c = new VBox(6);
        c.setPadding(new Insets(16));
        c.setStyle("-fx-background-color:" + bg + ";-fx-border-color:#D5D8DC;-fx-border-radius:5;-fx-background-radius:5;");
        HBox.setHgrow(c, Priority.ALWAYS);
        Label n = new Label(name);
        n.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        Label p = new Label(price + " / night");
        p.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        Label cap2 = new Label(cap);
        cap2.setStyle("-fx-font-size:11px;-fx-text-fill:#717D7E;");
        c.getChildren().addAll(n, p, cap2);
        return c;
    }

    private VBox svcCard(String name, String charge) {
        VBox c = new VBox(5);
        c.setPadding(new Insets(14));
        c.setStyle("-fx-background-color:white;-fx-border-color:#D5D8DC;-fx-border-radius:5;-fx-background-radius:5;");
        HBox.setHgrow(c, Priority.ALWAYS);
        Label n = new Label(name);
        n.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1C2833;");
        Label ch = new Label(charge);
        ch.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1E8449;");
        Label ps = new Label("per stay");
        ps.setStyle("-fx-font-size:10px;-fx-text-fill:#717D7E;");
        c.getChildren().addAll(n, ch, ps);
        return c;
    }

    private Label sh(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1C2833;");
        return l;
    }

    public void refresh() {
        totalRoomsVal.setText(String.valueOf(svc.getTotalRooms()));
        availableVal.setText(String.valueOf(svc.getAvailableRoomsCount()));
        occupiedVal.setText(String.valueOf(svc.getOccupiedRoomsCount()));
        bookingsVal.setText(String.valueOf(svc.getActiveBookingsCount()));
        revenueVal.setText(String.format("Rs %,.0f", svc.getTotalRevenue()));
    }
}
