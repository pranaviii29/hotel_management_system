package com.hotel.ui;

import com.hotel.model.*;
import com.hotel.service.HotelService;
import com.hotel.util.ReceiptGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;

public class CheckoutPanel extends VBox {

    private final HotelService svc;
    private Runnable onCheckout;

    private TableView<Booking> activeTable;
    private TableView<Booking> historyTable;
    private ObservableList<Booking> activeData;
    private ObservableList<Booking> historyData;
    private TextField tfSearch;

    public CheckoutPanel(Runnable onCheckout) {
        this.svc        = HotelService.getInstance();
        this.onCheckout = onCheckout;
        setStyle("-fx-background-color: #F2F3F5;");
        buildUI();
    }

    private void buildUI() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #F2F3F5; -fx-background-color: #F2F3F5;");

        VBox content = new VBox(24);
        content.setPadding(new Insets(30, 36, 36, 36));
        content.setStyle("-fx-background-color: #F2F3F5;");

        // ── Header ──────────────────────────────────────────────────────────
        VBox hdr = new VBox(3);
        Label title = new Label("Checkout & History");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#1C2833;");
        Label sub = new Label("Process guest checkouts and view past reservation history");
        sub.setStyle("-fx-font-size:12px;-fx-text-fill:#717D7E;");
        hdr.getChildren().addAll(title, sub);

        // ── Active / ready for checkout ──────────────────────────────────────
        Label actLabel = sh("Ready for Checkout");
        activeTable = buildActiveTable();
        VBox activeCard = new VBox(activeTable);
        activeCard.setStyle(UIStyles.TABLE);

        // ── History ──────────────────────────────────────────────────────────
        Label histLabel = sh("Checkout History");

        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 16, 12, 16));
        bar.setStyle(UIStyles.CARD);

        tfSearch = new TextField();
        tfSearch.setPromptText("Search by guest name, booking ID or room...");
        tfSearch.setStyle(UIStyles.FIELD); tfSearch.setPrefWidth(320);
        tfSearch.textProperty().addListener((o, ov, nv) -> filterHistory());

        Button btnRef = btn("Refresh", UIStyles.BTN_OUTLINE, UIStyles.BTN_OUTLINE_H);
        btnRef.setOnAction(e -> refresh());

        bar.getChildren().addAll(
            new Label("Search:"), tfSearch,
            new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
            btnRef
        );

        historyTable = buildHistoryTable();
        VBox histCard = new VBox(historyTable);
        histCard.setStyle(UIStyles.TABLE);

        content.getChildren().addAll(hdr, actLabel, activeCard, histLabel, bar, histCard);
        sp.setContent(content);
        VBox.setVgrow(sp, Priority.ALWAYS);
        getChildren().add(sp);
        refresh();
    }

    // ── Active bookings table (ready to checkout) ─────────────────────────────

    @SuppressWarnings("unchecked")
    private TableView<Booking> buildActiveTable() {
        TableView<Booking> t = new TableView<>();
        t.setStyle(UIStyles.TABLE);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(230);
        t.setPlaceholder(new Label("No active bookings to checkout."));

        TableColumn<Booking, Void> cAct = new TableColumn<>("Actions");
        cAct.setPrefWidth(190);
        cAct.setCellFactory(c -> new TableCell<>() {
            final Button bCO  = new Button("Checkout");
            final Button bRcp = new Button("Receipt");
            final HBox box    = new HBox(6, bCO, bRcp);
            {
                bCO.setStyle(UIStyles.BTN_DANGER);
                bCO.setOnMouseEntered(e -> bCO.setStyle(UIStyles.BTN_DANGER_H));
                bCO.setOnMouseExited(e  -> bCO.setStyle(UIStyles.BTN_DANGER));
                bCO.setOnAction(e -> handleCheckout(getTableView().getItems().get(getIndex())));

                bRcp.setStyle(UIStyles.BTN_SMALL);
                bRcp.setOnAction(e -> showReceipt(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        t.getColumns().addAll(
            col("Booking ID", b -> b.getBookingId(),                              150),
            col("Guest",      b -> b.getGuest().getName(),                        130),
            col("Room",       b -> "Room " + b.getRoom().getRoomNumber()
                                  + " (" + b.getRoom().getRoomType().getDisplayName() + ")", 160),
            col("Check-In",   b -> b.getCheckInDate().toString(),                 100),
            col("Check-Out",  b -> b.getCheckOutDate().toString(),                100),
            col("Nights",     b -> String.valueOf(b.getNights()),                   60),
            col("Billed Total",b -> String.format("Rs %,.2f", b.getTotalAmount()), 115),
            cAct
        );

        activeData = FXCollections.observableArrayList();
        t.setItems(activeData);
        return t;
    }

    // ── History table ─────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private TableView<Booking> buildHistoryTable() {
        TableView<Booking> t = new TableView<>();
        t.setStyle(UIStyles.TABLE);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(300);
        t.setPlaceholder(new Label("No checkout history yet."));

        TableColumn<Booking, String> cStat = col("Status",
            b -> b.getStatus().getDisplayName(), 90);
        cStat.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label l = new Label(item); l.setStyle(UIStyles.badge(item));
                setGraphic(l); setText(null);
            }
        });

        // Refund column — shows refund amount if any
        TableColumn<Booking, String> cRefund = col("Refund",
            b -> b.getRefundAmount() > 0
                ? "- Rs " + String.format("%,.2f", b.getRefundAmount())
                : "—", 100);
        cRefund.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setStyle(item.startsWith("-")
                    ? "-fx-text-fill:#1E8449;-fx-font-weight:bold;"
                    : "-fx-text-fill:#717D7E;");
            }
        });

        TableColumn<Booking, Void> cRcp = new TableColumn<>("Receipt");
        cRcp.setPrefWidth(75);
        cRcp.setCellFactory(c -> new TableCell<>() {
            final Button b = new Button("View");
            { b.setStyle(UIStyles.BTN_SMALL);
              b.setOnAction(e -> showReceipt(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : b);
            }
        });

        t.getColumns().addAll(
            col("Booking ID",  b -> b.getBookingId(),                              150),
            col("Guest",       b -> b.getGuest().getName(),                        130),
            col("Room",        b -> b.getRoom().getRoomNumber()
                                   + " — " + b.getRoom().getRoomType().getDisplayName(), 160),
            col("Check-Out",   b -> b.getCheckOutDate().toString(),                100),
            col("Nights",      b -> String.valueOf(b.getNights()),                   60),
            col("Billed",      b -> String.format("Rs %,.2f", b.getTotalAmount()),  105),
            cRefund,
            col("Charged",     b -> String.format("Rs %,.2f", b.getFinalAmountAfterRefund()), 110),
            cStat, cRcp
        );

        historyData = FXCollections.observableArrayList();
        t.setItems(historyData);
        return t;
    }

    // ── Checkout flow ─────────────────────────────────────────────────────────

    private void handleCheckout(Booking booking) {

        // ── Step 1: Check for incomplete services and preview the bill ─────────
        List<ServiceRequest> allRequests = booking.getServiceRequests();
        List<ServiceRequest> incomplete  = allRequests.stream()
            .filter(r -> !r.isCompleted()).toList();
        List<ServiceRequest> completed   = allRequests.stream()
            .filter(ServiceRequest::isCompleted).toList();

        // Build confirmation content
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("Guest  : %s\n", booking.getGuest().getName()));
        msg.append(String.format("Room   : Room %s — %s\n",
            booking.getRoom().getRoomNumber(),
            booking.getRoom().getRoomType().getDisplayName()));
        msg.append(String.format("Nights : %d  (%s  →  %s)\n\n",
            booking.getNights(), booking.getCheckInDate(), booking.getCheckOutDate()));

        msg.append(String.format("  Room Charge     :  Rs %,.2f\n",
            booking.getRoomChargeOnly()));

        // Completed services
        if (!completed.isEmpty()) {
            msg.append("  Completed Services:\n");
            for (ServiceRequest r : completed) {
                msg.append(String.format("    %-20s  Rs %,.2f\n",
                    r.getServiceType().getDisplayName(), r.getServiceType().getCharge()));
            }
        }

        // Incomplete services — highlight as refund
        double previewRefund = 0;
        if (!incomplete.isEmpty()) {
            msg.append("\n  INCOMPLETE SERVICES (will be refunded):\n");
            for (ServiceRequest r : incomplete) {
                double svcCharge  = r.getServiceType().getCharge();
                double gstOnSvc   = svcCharge * 0.18;
                double refundThis = svcCharge + gstOnSvc;
                previewRefund    += refundThis;
                msg.append(String.format("    %-20s  Rs %,.2f  [REFUND: Rs %,.2f]\n",
                    r.getServiceType().getDisplayName(), svcCharge, refundThis));
            }
        }

        msg.append(String.format("\n  GST (18%%)       :  Rs %,.2f\n", booking.getGstAmount()));
        msg.append(String.format("  Original Total  :  Rs %,.2f\n", booking.getTotalAmount()));

        if (previewRefund > 0) {
            msg.append(String.format("  Total Refund    :  Rs %,.2f\n", previewRefund));
            msg.append(String.format("  AMOUNT TO PAY   :  Rs %,.2f\n",
                booking.getTotalAmount() - previewRefund));
        }

        msg.append("\nProceed with checkout?");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Checkout");
        confirm.setHeaderText(incomplete.isEmpty()
            ? "Checkout — " + booking.getBookingId()
            : "Checkout with Refund — " + booking.getBookingId());
        confirm.setContentText(msg.toString());
        confirm.getDialogPane().setStyle("-fx-font-size: 13px;");
        confirm.getDialogPane().setPrefWidth(500);

        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> doCheckout(booking));
    }

    private void doCheckout(Booking booking) {
        HotelService.CheckoutResult result = svc.checkOut(booking.getBookingId());

        if (!result.success) {
            alert("Error", result.message);
            return;
        }

        // ── Show refund result dialog ──────────────────────────────────────────
        if (result.hasRefund()) {
            showRefundSummary(result.booking);
        }

        // ── Always show receipt ────────────────────────────────────────────────
        showReceipt(result.booking);

        if (onCheckout != null) onCheckout.run();
        refresh();
    }

    /**
     * Shows a dedicated refund summary dialog so the checkout staff can
     * clearly communicate the refund to the guest.
     */
    private void showRefundSummary(Booking booking) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Refund Applied");
        dlg.setHeaderText(null);

        DialogPane dp = dlg.getDialogPane();
        dp.setStyle("-fx-background-color: white;");
        dp.setPrefWidth(480);
        dp.getButtonTypes().add(ButtonType.CLOSE);
        dp.lookupButton(ButtonType.CLOSE).setStyle(UIStyles.BTN_PRIMARY);

        VBox box = new VBox(16);
        box.setPadding(new Insets(24));

        // ── Title row ──────────────────────────────────────────────────────────
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("↩");
        icon.setStyle("-fx-font-size:28px;-fx-text-fill:#1E8449;");
        VBox titleBox = new VBox(2);
        Label titleLabel = new Label("Refund Applied at Checkout");
        titleLabel.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1C2833;");
        Label guestLabel = new Label("Guest: " + booking.getGuest().getName()
            + "  |  Room: " + booking.getRoom().getRoomNumber()
            + "  |  " + booking.getBookingId());
        guestLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#717D7E;");
        titleBox.getChildren().addAll(titleLabel, guestLabel);
        titleRow.getChildren().addAll(icon, titleBox);

        Separator sep1 = new Separator();

        // ── Refunded services list ─────────────────────────────────────────────
        Label refundHeading = new Label("The following services were not completed and have been refunded:");
        refundHeading.setStyle("-fx-font-size:12px;-fx-text-fill:#717D7E;-fx-wrap-text:true;");
        refundHeading.setMaxWidth(430);

        VBox refundList = new VBox(8);
        refundList.setPadding(new Insets(10, 14, 10, 14));
        refundList.setStyle(
            "-fx-background-color:#F0FBF4;" +
            "-fx-border-color:#A9DFBF;" +
            "-fx-border-radius:5;" +
            "-fx-background-radius:5;");

        for (ServiceType svc : booking.getRefundedServices()) {
            double svcCharge   = svc.getCharge();
            double gstOnSvc    = svcCharge * 0.18;
            double totalRefund = svcCharge + gstOnSvc;

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);

            Label svcName = new Label(svc.getDisplayName());
            svcName.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1C2833;");
            HBox.setHgrow(svcName, Priority.ALWAYS);

            VBox amountBox = new VBox(1);
            amountBox.setAlignment(Pos.CENTER_RIGHT);
            Label svcAmt = new Label(String.format("Rs %,.2f + GST Rs %,.2f", svcCharge, gstOnSvc));
            svcAmt.setStyle("-fx-font-size:11px;-fx-text-fill:#717D7E;");
            Label refundAmt = new Label(String.format("Refund: Rs %,.2f", totalRefund));
            refundAmt.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1E8449;");
            amountBox.getChildren().addAll(svcAmt, refundAmt);

            row.getChildren().addAll(svcName, amountBox);
            refundList.getChildren().add(row);
        }

        Separator sep2 = new Separator();

        // ── Summary amounts ────────────────────────────────────────────────────
        GridPane summary = new GridPane();
        summary.setHgap(16); summary.setVgap(8);
        summary.setPadding(new Insets(8, 0, 0, 0));

        addSummaryRow(summary, 0, "Original Bill:",
            String.format("Rs %,.2f", booking.getTotalAmount()), false);
        addSummaryRow(summary, 1, "Total Refund:",
            "− Rs " + String.format("%,.2f", booking.getRefundAmount()), true);
        addSummaryRow(summary, 2, "Amount Charged:",
            String.format("Rs %,.2f", booking.getFinalAmountAfterRefund()), false);

        // Highlighted final amount
        HBox finalRow = new HBox();
        finalRow.setAlignment(Pos.CENTER_LEFT);
        finalRow.setPadding(new Insets(10, 14, 10, 14));
        finalRow.setStyle(
            "-fx-background-color:#1E2A38;" +
            "-fx-background-radius:5;");
        Label finalKey = new Label("AMOUNT CHARGED TO GUEST:");
        finalKey.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#BDC3C7;");
        HBox.setHgrow(finalKey, Priority.ALWAYS);
        Label finalVal = new Label(String.format("Rs %,.2f", booking.getFinalAmountAfterRefund()));
        finalVal.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:white;");
        finalRow.getChildren().addAll(finalKey, finalVal);

        box.getChildren().addAll(
            titleRow, sep1,
            refundHeading, refundList,
            sep2, summary, finalRow
        );

        dp.setContent(box);
        dlg.showAndWait();
    }

    private void addSummaryRow(GridPane g, int row, String key, String val, boolean green) {
        Label k = new Label(key);
        k.setStyle("-fx-font-size:13px;-fx-text-fill:#717D7E;-fx-min-width:160;");
        Label v = new Label(val);
        v.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"
            + (green ? "#1E8449" : "#1C2833") + ";");
        g.add(k, 0, row); g.add(v, 1, row);
    }

    private void showReceipt(Booking booking) {
        String text = ReceiptGenerator.generateReceipt(booking);
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Receipt — " + booking.getBookingId());
        DialogPane dp = dlg.getDialogPane();
        dp.setStyle("-fx-background-color:white;"); dp.setPrefWidth(540);
        dp.getButtonTypes().add(ButtonType.CLOSE);
        dp.lookupButton(ButtonType.CLOSE).setStyle(UIStyles.BTN_OUTLINE);

        VBox box = new VBox(10); box.setPadding(new Insets(20));
        TextArea ta = new TextArea(text);
        ta.setEditable(false); ta.setPrefHeight(460);
        ta.setStyle(
            "-fx-font-family:'Courier New',monospace;" +
            "-fx-font-size:12px;" +
            "-fx-background-color:#F8F9FA;" +
            "-fx-border-color:#D5D8DC;");
        Label saved = new Label("Saved: " + System.getProperty("user.home")
            + "/SilverviewHMS/receipts/" + booking.getBookingId() + ".txt");
        saved.setStyle("-fx-font-size:11px;-fx-text-fill:#717D7E;");
        box.getChildren().addAll(ta, saved);
        dp.setContent(box);
        dlg.showAndWait();
    }

    private void filterHistory() {
        String q = tfSearch.getText().toLowerCase();
        List<Booking> all = svc.getBookingHistory();
        historyData.setAll(q.isBlank() ? all : all.stream().filter(b ->
            b.getBookingId().toLowerCase().contains(q) ||
            b.getGuest().getName().toLowerCase().contains(q) ||
            b.getRoom().getRoomNumber().toLowerCase().contains(q)).toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TableColumn<Booking, String> col(String name,
            java.util.function.Function<Booking, String> fn, double w) {
        TableColumn<Booking, String> c = new TableColumn<>(name);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w); return c;
    }

    private Button btn(String t, String s, String sh) {
        Button b = new Button(t); b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(sh)); b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }

    private Label sh(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1C2833;");
        return l;
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.getDialogPane().setStyle("-fx-font-size:13px;"); a.showAndWait();
    }

    public void refresh() {
        activeData.setAll(svc.getActiveBookings());
        filterHistory();
    }
}
