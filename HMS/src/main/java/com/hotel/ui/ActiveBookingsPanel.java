package com.hotel.ui;

import com.hotel.model.*;
import com.hotel.service.HotelService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ActiveBookingsPanel extends VBox {

    private final HotelService svc;
    private TableView<Booking> table;
    private ObservableList<Booking> data;
    private TextField tfSearch;

    public ActiveBookingsPanel() {
        this.svc = HotelService.getInstance();
        setStyle("-fx-background-color: #F2F3F5;");
        buildUI();
    }

    private void buildUI() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true); sp.setFitToHeight(true);
        sp.setStyle("-fx-background: #F2F3F5; -fx-background-color: #F2F3F5;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 36, 36, 36));
        content.setStyle("-fx-background-color: #F2F3F5;");

        // ── Header ──────────────────────────────────────────────────────────
        HBox hdr = new HBox();
        hdr.setAlignment(Pos.CENTER_LEFT);
        VBox tbox = new VBox(3);
        Label title = new Label("Active Bookings");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1C2833;");
        Label sub = new Label("View all confirmed reservations and guest service selections");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #717D7E;");
        tbox.getChildren().addAll(title, sub);
        HBox.setHgrow(tbox, Priority.ALWAYS);

        Button btnRef = btn("Refresh", UIStyles.BTN_OUTLINE, UIStyles.BTN_OUTLINE_H);
        btnRef.setOnAction(e -> refresh());
        hdr.getChildren().addAll(tbox, btnRef);

        // ── Search bar ──────────────────────────────────────────────────────
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle(UIStyles.CARD);
        tfSearch = new TextField();
        tfSearch.setPromptText("Search by booking ID, guest name or room number...");
        tfSearch.setStyle(UIStyles.FIELD);
        tfSearch.setPrefWidth(360);
        tfSearch.textProperty().addListener((o, ov, nv) -> filter());
        bar.getChildren().addAll(new Label("Search:"), tfSearch);

        // ── Table ───────────────────────────────────────────────────────────
        table = buildTable();
        VBox tableCard = new VBox(table);
        tableCard.setStyle(UIStyles.TABLE);

        content.getChildren().addAll(hdr, bar, tableCard);
        sp.setContent(content);
        VBox.setVgrow(sp, Priority.ALWAYS);
        getChildren().add(sp);
        refresh();
    }

    @SuppressWarnings("unchecked")
    private TableView<Booking> buildTable() {
        TableView<Booking> t = new TableView<>();
        t.setStyle(UIStyles.TABLE);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(520);
        t.setPlaceholder(new Label("No active bookings found."));

        // Status badge column
        TableColumn<Booking, String> cStatus = col("Status", b -> b.getStatus().getDisplayName(), 90);
        cStatus.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label l = new Label(item);
                l.setStyle(UIStyles.badge(item));
                setGraphic(l); setText(null);
            }
        });

        // Services booked column — shows what the guest chose at booking time
        TableColumn<Booking, String> cSvc = col("Services Booked", b -> {
            List<ServiceType> svcs = b.getServices();
            if (svcs == null || svcs.isEmpty()) return "None";
            return svcs.stream()
                       .map(ServiceType::getDisplayName)
                       .reduce((a, x) -> a + ", " + x)
                       .orElse("None");
        }, 200);

        // Details action column — only Details, no Request Service
        TableColumn<Booking, Void> cAct = new TableColumn<>("Actions");
        cAct.setPrefWidth(90);
        cAct.setCellFactory(c -> new TableCell<>() {
            final Button bView = smallBtn("Details");
            {
                bView.setOnAction(e -> showDetails(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : bView);
            }
        });

        t.getColumns().addAll(
            col("Booking ID",  b -> b.getBookingId(),                           150),
            col("Guest Name",  b -> b.getGuest().getName(),                     130),
            col("Phone",       b -> b.getGuest().getPhone(),                    110),
            col("Room",        b -> "Room " + b.getRoom().getRoomNumber()
                                   + " (" + b.getRoom().getRoomType().getDisplayName() + ")", 160),
            col("Check-In",    b -> b.getCheckInDate().toString(),              100),
            col("Check-Out",   b -> b.getCheckOutDate().toString(),             100),
            col("Nights",      b -> String.valueOf(b.getNights()),               60),
            col("Total",       b -> String.format("Rs %,.2f", b.getTotalAmount()), 110),
            cStatus,
            cSvc,
            cAct
        );

        data = FXCollections.observableArrayList();
        t.setItems(data);
        return t;
    }

    private void showDetails(Booking b) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Booking Details — " + b.getBookingId());
        DialogPane dp = dlg.getDialogPane();
        dp.setStyle("-fx-background-color: white;");
        dp.setPrefWidth(500);
        dp.getButtonTypes().add(ButtonType.CLOSE);
        dp.lookupButton(ButtonType.CLOSE).setStyle(UIStyles.BTN_OUTLINE);

        VBox box = new VBox(14);
        box.setPadding(new Insets(20));

        Label h = new Label("Booking: " + b.getBookingId());
        h.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1C2833;");

        GridPane g = new GridPane();
        g.setHgap(16); g.setVgap(9);
        g.setPadding(new Insets(14));
        g.setStyle("-fx-background-color:#F8F9FA;-fx-background-radius:5;");

        int r = 0;
        addRow(g, r++, "Guest Name",   b.getGuest().getName());
        addRow(g, r++, "Phone",        b.getGuest().getPhone());
        addRow(g, r++, "Email",        b.getGuest().getEmail().isBlank() ? "—" : b.getGuest().getEmail());
        addRow(g, r++, "ID Proof",     b.getGuest().getIdProofType() + " — " + b.getGuest().getIdProofNumber());
        addRow(g, r++, "Address",      b.getGuest().getAddress().isBlank() ? "—" : b.getGuest().getAddress());
        addRow(g, r++, "Room",         "Room " + b.getRoom().getRoomNumber() + " — " + b.getRoom().getRoomType().getDisplayName());
        addRow(g, r++, "Floor",        b.getRoom().getFloor());
        addRow(g, r++, "Check-In",     b.getCheckInDate().toString());
        addRow(g, r++, "Check-Out",    b.getCheckOutDate().toString());
        addRow(g, r++, "Nights",       String.valueOf(b.getNights()));
        addRow(g, r++, "Room Charge",  String.format("Rs %,.2f  (Rs %,.0f x %d nights)",
                                        b.getRoomChargeOnly(), b.getRoom().getPricePerNight(), b.getNights()));

        // Services booked
        String svcText = b.getServices().isEmpty() ? "None"
            : b.getServices().stream().map(st -> st.getDisplayName() + " (Rs " +
              String.format("%,.0f", st.getCharge()) + ")")
              .reduce((a, x) -> a + "\n  " + x).orElse("None");
        addRow(g, r++, "Services",     svcText);
        addRow(g, r++, "Service Total",String.format("Rs %,.2f", b.getServiceAmount()));
        addRow(g, r++, "GST (18%)",    String.format("Rs %,.2f", b.getGstAmount()));
        addRow(g, r++, "TOTAL",        String.format("Rs %,.2f", b.getTotalAmount()));
        addRow(g, r++, "Status",       b.getStatus().getDisplayName());

        if (b.getSpecialRequests() != null && !b.getSpecialRequests().isBlank())
            addRow(g, r++, "Special Requests", b.getSpecialRequests());

        box.getChildren().addAll(h, g);

        // Show service requests if any
        if (!b.getServiceRequests().isEmpty()) {
            Label svcTitle = new Label("Service Requests for this Booking");
            svcTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1C2833;");

            VBox svcBox = new VBox(6);
            svcBox.setPadding(new Insets(10));
            svcBox.setStyle("-fx-background-color:#F8F9FA;-fx-background-radius:5;");

            for (ServiceRequest req : b.getServiceRequests()) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                Label svcName = new Label(req.getServiceType().getDisplayName());
                svcName.setStyle("-fx-font-size: 12px; -fx-text-fill: #1C2833; -fx-min-width: 130;");
                Label charge = new Label("Rs " + String.format("%,.0f", req.getServiceType().getCharge()));
                charge.setStyle("-fx-font-size: 12px; -fx-text-fill: #1E8449;");
                Label statusBadge = new Label(req.getStatus());
                statusBadge.setStyle(UIStyles.badge(req.getStatus()));
                Label at = new Label("Requested: " + req.getRequestedAt());
                at.setStyle("-fx-font-size: 11px; -fx-text-fill: #717D7E;");
                row.getChildren().addAll(svcName, charge, statusBadge, at);
                svcBox.getChildren().add(row);
            }
            box.getChildren().addAll(svcTitle, svcBox);
        }

        dp.setContent(box);
        dlg.showAndWait();
    }

    private void addRow(GridPane g, int r, String key, String val) {
        Label k = new Label(key + ":");
        k.setStyle("-fx-font-size: 12px; -fx-text-fill: #717D7E; -fx-min-width: 120;");
        Label v = new Label(val);
        v.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1C2833; -fx-wrap-text: true;");
        v.setMaxWidth(280);
        g.add(k, 0, r); g.add(v, 1, r);
    }

    private void filter() {
        String q = tfSearch.getText().toLowerCase();
        List<Booking> all = svc.getActiveBookings();
        data.setAll(q.isBlank() ? all : all.stream().filter(b ->
            b.getBookingId().toLowerCase().contains(q) ||
            b.getGuest().getName().toLowerCase().contains(q) ||
            b.getRoom().getRoomNumber().toLowerCase().contains(q)).toList());
    }

    private TableColumn<Booking, String> col(String name, java.util.function.Function<Booking, String> fn, double w) {
        TableColumn<Booking, String> c = new TableColumn<>(name);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w);
        return c;
    }

    private Button btn(String t, String s, String sh) {
        Button b = new Button(t); b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(sh)); b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }

    private Button smallBtn(String text) {
        Button b = new Button(text); b.setStyle(UIStyles.BTN_SMALL); return b;
    }

    public void refresh() { data.setAll(svc.getActiveBookings()); }
}
