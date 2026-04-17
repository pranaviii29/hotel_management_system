package com.hotel.ui;

import com.hotel.model.ServiceRequest;
import com.hotel.model.ServiceType;
import com.hotel.service.HotelService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ServiceRequestsPanel extends VBox {

    private final HotelService svc;
    private TableView<ServiceRequest> table;
    private ObservableList<ServiceRequest> data;
    private ComboBox<String> cbStatus;
    private TextField tfSearch;

    private Label lblTotal, lblPending, lblCompleted;

    public ServiceRequestsPanel() {
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
        Label title = new Label("Service Requests");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1C2833;");
        Label sub = new Label("All service requests across every booking — mark them as completed here");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #717D7E;");
        tbox.getChildren().addAll(title, sub);
        HBox.setHgrow(tbox, Priority.ALWAYS);
        Button btnRef = btn("Refresh", UIStyles.BTN_OUTLINE, UIStyles.BTN_OUTLINE_H);
        btnRef.setOnAction(e -> refresh());
        hdr.getChildren().addAll(tbox, btnRef);

        // ── Stat cards ───────────────────────────────────────────────────────
        lblTotal     = statVal();
        lblPending   = statVal();
        lblCompleted = statVal();

        HBox stats = new HBox(14);
        stats.getChildren().addAll(
            statCard("TOTAL REQUESTS", lblTotal,     "#2C3E50"),
            statCard("PENDING",        lblPending,   "#E67E22"),
            statCard("COMPLETED",      lblCompleted, "#1E8449")
        );

        // ── Filter bar ──────────────────────────────────────────────────────
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle(UIStyles.CARD);

        tfSearch = new TextField();
        tfSearch.setPromptText("Search by guest name, room, booking ID or service...");
        tfSearch.setStyle(UIStyles.FIELD); tfSearch.setPrefWidth(320);
        tfSearch.textProperty().addListener((o, ov, nv) -> applyFilters());

        cbStatus = new ComboBox<>(FXCollections.observableArrayList(
            "All", "Pending", "Completed", "Not Completed"));  // ← added Not Completed filter
        cbStatus.setValue("All");
        cbStatus.setStyle(UIStyles.COMBO); cbStatus.setPrefWidth(150);
        cbStatus.setOnAction(e -> applyFilters());

        bar.getChildren().addAll(
            new Label("Search:"), tfSearch,
            new Label("Status:"), cbStatus
        );

        // ── Table ───────────────────────────────────────────────────────────
        table = buildTable();
        VBox tableCard = new VBox(table);
        tableCard.setStyle(UIStyles.TABLE);

        content.getChildren().addAll(hdr, stats, bar, tableCard);
        sp.setContent(content);
        VBox.setVgrow(sp, Priority.ALWAYS);
        getChildren().add(sp);
        refresh();
    }

    @SuppressWarnings("unchecked")
    private TableView<ServiceRequest> buildTable() {
        TableView<ServiceRequest> t = new TableView<>();
        t.setStyle(UIStyles.TABLE);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(480);
        t.setPlaceholder(new Label("No service requests found."));

        // Status badge column
        TableColumn<ServiceRequest, String> cStat = col("Status", r -> r.getStatus(), 110);
        cStat.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label l = new Label(item);
                l.setStyle(UIStyles.badge(item));
                setGraphic(l); setText(null);
            }
        });

        // ── Action column ────────────────────────────────────────────────────
        TableColumn<ServiceRequest, Void> cAct = new TableColumn<>("Action");
        cAct.setPrefWidth(160);
        cAct.setCellFactory(c -> new TableCell<>() {

            // "Mark Completed" button — shown for Pending requests
            final Button btnMark = new Button("Mark Completed");

            // "Not Completed" label — shown for checked-out but incomplete requests
            final Label lblNotDone = new Label("✗  Not Completed");

            {
                // Style: Mark Completed button (green)
                btnMark.setStyle(
                    "-fx-background-color:#1E8449;-fx-text-fill:white;" +
                    "-fx-font-size:11px;-fx-padding:5 12;" +
                    "-fx-background-radius:4;-fx-cursor:hand;");
                btnMark.setOnMouseEntered(e -> btnMark.setStyle(
                    "-fx-background-color:#196F3D;-fx-text-fill:white;" +
                    "-fx-font-size:11px;-fx-padding:5 12;" +
                    "-fx-background-radius:4;-fx-cursor:hand;"));
                btnMark.setOnMouseExited(e -> btnMark.setStyle(
                    "-fx-background-color:#1E8449;-fx-text-fill:white;" +
                    "-fx-font-size:11px;-fx-padding:5 12;" +
                    "-fx-background-radius:4;-fx-cursor:hand;"));
                btnMark.setOnAction(e -> {
                    ServiceRequest req = getTableView().getItems().get(getIndex());
                    if (!req.isCompleted()) {
                        svc.markServiceCompleted(req);
                        refresh();
                    }
                });

                // Style: Not Completed label (red pill)
                lblNotDone.setStyle(
                    "-fx-background-color:#FADBD8;-fx-text-fill:#922B21;" +
                    "-fx-font-size:11px;-fx-font-weight:bold;" +
                    "-fx-padding:5 10;-fx-background-radius:4;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                ServiceRequest req = getTableView().getItems().get(getIndex());

                if (req.isCompleted()) {
                    // Already done — show nothing
                    setGraphic(null);
                } else if (req.isGuestCheckedOut()) {
                    // Guest left without service being done — show red label
                    setGraphic(lblNotDone);
                } else {
                    // Still active booking, service pending — show button
                    setGraphic(btnMark);
                }
            }
        });

        t.getColumns().addAll(
            col("Request ID",   r -> r.getRequestId(),                              150),
            col("Booking ID",   r -> r.getBookingId(),                              150),
            col("Guest Name",   r -> r.getGuestName(),                              130),
            col("Room",         r -> r.getRoomNumber(),                              70),
            col("Service",      r -> r.getServiceType().getDisplayName(),           130),
            col("Charge",       r -> "Rs " + String.format("%,.0f",
                                     r.getServiceType().getCharge()),                90),
            col("Requested At", r -> r.getRequestedAt(),                            130),
            cStat,
            col("Completed At", r -> r.getCompletedAt() != null
                                     ? r.getCompletedAt() : "—",                   130),
            cAct
        );

        data = FXCollections.observableArrayList();
        t.setItems(data);
        return t;
    }

    // ── Stat card builder ─────────────────────────────────────────────────────

    private VBox statCard(String tag, Label val, String color) {
        VBox c = new VBox(6);
        c.setPadding(new Insets(18));
        c.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:#D5D8DC;" +
            "-fx-border-radius:5;" +
            "-fx-background-radius:5;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),6,0,0,2);");
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

    private Label statVal() {
        Label l = new Label("0");
        l.setStyle("-fx-font-size:28px;-fx-font-weight:bold;");
        return l;
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    private void applyFilters() {
        String q      = tfSearch.getText().toLowerCase();
        String status = cbStatus.getValue();
        List<ServiceRequest> all = svc.getAllServiceRequests();

        data.setAll(all.stream().filter(r -> {
            boolean mq = q.isBlank()
                || r.getGuestName().toLowerCase().contains(q)
                || r.getRoomNumber().toLowerCase().contains(q)
                || r.getBookingId().toLowerCase().contains(q)
                || r.getServiceType().getDisplayName().toLowerCase().contains(q);
            boolean ms = "All".equals(status)
                || ("Pending".equals(status)        && !r.isCompleted() && !r.isGuestCheckedOut())
                || ("Completed".equals(status)      &&  r.isCompleted())
                || ("Not Completed".equals(status)  && !r.isCompleted() &&  r.isGuestCheckedOut());
            return mq && ms;
        }).toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TableColumn<ServiceRequest, String> col(String name,
            java.util.function.Function<ServiceRequest, String> fn, double w) {
        TableColumn<ServiceRequest, String> c = new TableColumn<>(name);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w);
        return c;
    }

    private Button btn(String t, String s, String sh) {
        Button b = new Button(t); b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(sh)); b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }

    public void refresh() {
        applyFilters();
        updateStats();
    }

    private void updateStats() {
        List<ServiceRequest> all = svc.getAllServiceRequests();
        long pending      = all.stream()
            .filter(r -> !r.isCompleted() && !r.isGuestCheckedOut()).count();
        long completed    = all.stream().filter(ServiceRequest::isCompleted).count();
        lblTotal.setText(String.valueOf(all.size()));
        lblPending.setText(String.valueOf(pending));
        lblCompleted.setText(String.valueOf(completed));
    }
}