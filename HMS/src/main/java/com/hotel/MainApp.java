package com.hotel;

import com.hotel.service.AutoSaveService;
import com.hotel.ui.*;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    private StackPane contentArea;
    private Label statusLabel;
    private Label activeNavLabel;

    private DashboardPanel      dashboardPanel;
    private RoomsPanel          roomsPanel;
    private BookingPanel        bookingPanel;
    private ActiveBookingsPanel activeBookingsPanel;
    private CheckoutPanel       checkoutPanel;
    private ServiceRequestsPanel serviceRequestsPanel;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Silverview Grand — Hotel Management System");
        stage.setMinWidth(1100); stage.setMinHeight(700);

        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setTop(buildTopBar());
        root.setBottom(buildStatusBar());

        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #F2F3F5;");
        root.setCenter(contentArea);

        navigate("dashboard");

        AutoSaveService.getInstance().startAutoSave();
        AutoSaveService.getInstance().statusMessageProperty().addListener(
            (obs, o, n) -> Platform.runLater(() -> statusLabel.setText(n)));

        Scene scene = new Scene(root, 1300, 800);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> { AutoSaveService.getInstance().shutdown(); Platform.exit(); });
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #1E2A38;");

        // Brand block
        VBox brand = new VBox(4);
        brand.setPadding(new Insets(24, 20, 18, 20));
        brand.setStyle("-fx-background-color: #17202A;");
        Label h1 = new Label("SILVERVIEW");
        h1.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label h2 = new Label("GRAND HOTEL");
        h2.setStyle("-fx-font-size:9px;-fx-text-fill:#717D7E;-fx-letter-spacing:1.5;");
        Region accent = new Region();
        accent.setPrefHeight(2); accent.setMaxWidth(36);
        accent.setStyle("-fx-background-color:#B7950B;-fx-background-radius:1;");
        Label h3 = new Label("Management System");
        h3.setStyle("-fx-font-size:10px;-fx-text-fill:#566573;");
        brand.getChildren().addAll(h1, h2, accent, h3);

        // Nav items
        VBox nav = new VBox(0);
        nav.setPadding(new Insets(10, 0, 0, 0));
        VBox.setVgrow(nav, Priority.ALWAYS);

        nav.getChildren().addAll(
            navSection("OVERVIEW"),
            navItem("Dashboard",          "dashboard"),
            navSection("ROOMS"),
            navItem("Room Management",    "rooms"),
            navSection("RESERVATIONS"),
            navItem("New Booking",        "booking"),
            navItem("Active Bookings",    "active"),
            navSection("OPERATIONS"),
            navItem("Checkout & History", "checkout"),
            navItem("Service Requests",   "services")
        );

        // Version
        VBox bottom = new VBox();
        bottom.setPadding(new Insets(14, 20, 14, 20));
        VBox.setVgrow(bottom, Priority.ALWAYS);
        bottom.setAlignment(Pos.BOTTOM_LEFT);
        Label ver = new Label("v2.0.0  |  Silverview HMS");
        ver.setStyle("-fx-font-size:10px;-fx-text-fill:#566573;");
        bottom.getChildren().add(ver);

        sidebar.getChildren().addAll(brand, nav, bottom);
        return sidebar;
    }

    private Label navSection(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:9px;-fx-text-fill:#566573;-fx-font-weight:bold;");
        l.setPadding(new Insets(14, 20, 3, 20));
        return l;
    }

    private Label navItem(String text, String key) {
        Label l = new Label(text);
        l.setPrefWidth(220);
        l.setPadding(new Insets(11, 20, 11, 22));
        l.setStyle("-fx-font-size:13px;-fx-text-fill:#BDC3C7;-fx-cursor:hand;");

        l.setOnMouseEntered(e -> { if (l != activeNavLabel) l.setStyle("-fx-font-size:13px;-fx-text-fill:white;-fx-background-color:#2C3E50;-fx-cursor:hand;"); });
        l.setOnMouseExited(e  -> { if (l != activeNavLabel) l.setStyle("-fx-font-size:13px;-fx-text-fill:#BDC3C7;-fx-cursor:hand;"); });
        l.setOnMouseClicked(e -> { setActive(l); navigate(key); });

        if (key.equals("dashboard")) setActive(l);
        return l;
    }

    private void setActive(Label l) {
        if (activeNavLabel != null)
            activeNavLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#BDC3C7;-fx-cursor:hand;");
        activeNavLabel = l;
        l.setStyle("-fx-font-size:13px;-fx-text-fill:white;-fx-background-color:#17202A;" +
                   "-fx-border-color:#B7950B;-fx-border-width:0 0 0 3;-fx-cursor:hand;");
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setPrefHeight(54);
        bar.setStyle("-fx-background-color:white;-fx-border-color:#D5D8DC;-fx-border-width:0 0 1 0;");

        Label name = new Label("Silverview Grand Hotel");
        name.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#1C2833;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label clock = new Label();
        clock.setStyle("-fx-font-size:12px;-fx-text-fill:#717D7E;");
        updateClock(clock);

        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.seconds(1), e -> updateClock(clock)));
        tl.setCycleCount(javafx.animation.Animation.INDEFINITE);
        tl.play();

        bar.getChildren().addAll(name, sp, clock);
        return bar;
    }

    private void updateClock(Label l) {
        l.setText(java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  |  HH:mm:ss")));
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private HBox buildStatusBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(5, 20, 5, 20));
        bar.setStyle("-fx-background-color:#F8F9FA;-fx-border-color:#D5D8DC;-fx-border-width:1 0 0 0;");

        Rectangle dot = new Rectangle(8, 8);
        dot.setArcWidth(8); dot.setArcHeight(8); dot.setFill(Color.web("#27AE60"));

        statusLabel = new Label("System ready — auto-save enabled (every 30 s)");
        statusLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#717D7E;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label path = new Label("Data: " + System.getProperty("user.home") + "/SilverviewHMS/");
        path.setStyle("-fx-font-size:10px;-fx-text-fill:#95A5A6;");

        bar.getChildren().addAll(dot, statusLabel, sp, path);
        return bar;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void navigate(String key) {
        Node panel = switch (key) {
            case "dashboard" -> getDashboard();
            case "rooms"     -> getRooms();
            case "booking"   -> getBooking();
            case "active"    -> getActive();
            case "checkout"  -> getCheckout();
            case "services"  -> getServices();
            default          -> getDashboard();
        };

        // Refresh before showing so data is always current
        switch (key) {
            case "dashboard" -> dashboardPanel.refresh();
            case "rooms"     -> roomsPanel.refresh();
            case "booking"   -> bookingPanel.refresh();
            case "active"    -> activeBookingsPanel.refresh();
            case "checkout"  -> checkoutPanel.refresh();
            case "services"  -> serviceRequestsPanel.refresh();
        }

        FadeTransition fade = new FadeTransition(Duration.millis(160), panel);
        fade.setFromValue(0); fade.setToValue(1);
        contentArea.getChildren().setAll(panel);
        fade.play();
    }

    private void refreshAll() {
        if (dashboardPanel      != null) dashboardPanel.refresh();
        if (roomsPanel          != null) roomsPanel.refresh();
        if (activeBookingsPanel != null) activeBookingsPanel.refresh();
        if (checkoutPanel       != null) checkoutPanel.refresh();
        if (serviceRequestsPanel!= null) serviceRequestsPanel.refresh();
    }

    // ── Lazy panel constructors ───────────────────────────────────────────────

    private DashboardPanel getDashboard() {
        if (dashboardPanel == null) dashboardPanel = new DashboardPanel();
        return dashboardPanel;
    }

    private RoomsPanel getRooms() {
        if (roomsPanel == null) roomsPanel = new RoomsPanel();
        return roomsPanel;
    }

    private BookingPanel getBooking() {
        if (bookingPanel == null) {
            bookingPanel = new BookingPanel(this::refreshAll);
        }
        return bookingPanel;
    }

    private ActiveBookingsPanel getActive() {
        if (activeBookingsPanel == null) activeBookingsPanel = new ActiveBookingsPanel();
        return activeBookingsPanel;
    }

    private CheckoutPanel getCheckout() {
        if (checkoutPanel == null) checkoutPanel = new CheckoutPanel(this::refreshAll);
        return checkoutPanel;
    }

    private ServiceRequestsPanel getServices() {
        if (serviceRequestsPanel == null) serviceRequestsPanel = new ServiceRequestsPanel();
        return serviceRequestsPanel;
    }

    public static void main(String[] args) { launch(args); }
}
