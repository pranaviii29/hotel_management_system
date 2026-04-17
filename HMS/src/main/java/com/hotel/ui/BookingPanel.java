package com.hotel.ui;

import com.hotel.model.*;
import com.hotel.service.AutoSaveService;
import com.hotel.service.HotelService;
import com.hotel.util.ReceiptGenerator;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * New Booking form panel.
 * All UIStyles constants used here are defined in UIStyles.java.
 */
public class BookingPanel extends VBox {

    private final HotelService svc;
    private final Runnable onBookingMade;

    // ── Guest fields ──────────────────────────────────────────────────────────
    private TextField tfName, tfPhone, tfEmail, tfAddress, tfIdNum;
    private Spinner<Integer> spAge;
    private ComboBox<String> cbIdType;

    // ── Room / stay fields ────────────────────────────────────────────────────
    private ComboBox<Room> cbRoom;
    private DatePicker dpIn, dpOut;
    private TextArea taSpecial;

    // ── Service checkboxes ────────────────────────────────────────────────────
    private CheckBox chkCleaning, chkLaundry, chkExtraBed, chkMiniBar;

    // ── Bill preview ──────────────────────────────────────────────────────────
    private Label lblRoomCharge, lblSvcCharge, lblGst, lblTotal;
    private VBox  billDetailsBox;
    private Label billPlaceholder;

    public BookingPanel(Runnable onBookingMade) {
        this.svc           = HotelService.getInstance();
        this.onBookingMade = onBookingMade;
        setStyle("-fx-background-color:#F2F3F5;");
        VBox.setVgrow(this, Priority.ALWAYS);
        buildUI();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UI construction
    // ══════════════════════════════════════════════════════════════════════════

    private void buildUI() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#F2F3F5;-fx-background-color:#F2F3F5;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox content = new VBox(22);
        content.setPadding(new Insets(30, 36, 36, 36));
        content.setStyle("-fx-background-color:#F2F3F5;");

        // Page header
        VBox hdr = new VBox(3);
        Label title = new Label("New Booking");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#1C2833;");
        Label sub = new Label("Fill in guest and room details to create a reservation");
        sub.setStyle("-fx-font-size:12px;-fx-text-fill:#717D7E;");
        hdr.getChildren().addAll(title, sub);

        // Two-column layout
        HBox cols = new HBox(20);
        VBox left  = new VBox(16);  HBox.setHgrow(left,  Priority.ALWAYS);
        VBox right = new VBox(14);  right.setMinWidth(280); right.setMaxWidth(300);

        left.getChildren().addAll(buildGuestCard(), buildRoomCard());
        right.getChildren().addAll(buildServicesCard(), buildBillCard(), buildButtons());

        cols.getChildren().addAll(left, right);
        content.getChildren().addAll(hdr, cols);
        scroll.setContent(content);
        getChildren().add(scroll);

        loadRooms();
    }

    // ── Guest information card ────────────────────────────────────────────────
    private VBox buildGuestCard() {
        VBox card = card("Guest Information");
        GridPane g = grid();

        tfName    = field("Enter full name");
        tfPhone   = field("Enter phone number");
        tfEmail   = field("Enter email address  (optional)");
        tfAddress = field("Enter home address  (optional)");
        tfIdNum   = field("Enter ID number");

        spAge = new Spinner<>(18, 100, 25);
        spAge.setEditable(true);
        spAge.setStyle(UIStyles.COMBO);        // COMBO == SPINNER alias
        spAge.setMaxWidth(Double.MAX_VALUE);

        cbIdType = new ComboBox<>(FXCollections.observableArrayList(
            "Aadhaar Card","PAN Card","Passport","Driving License","Voter ID"));
        cbIdType.setPromptText("Select ID type");
        cbIdType.setStyle(UIStyles.COMBO);
        cbIdType.setMaxWidth(Double.MAX_VALUE);

        int r = 0;
        g.add(lbl("Full Name *"),  0, r); g.add(tfName,    1, r++);
        g.add(lbl("Phone *"),      0, r); g.add(tfPhone,   1, r++);
        g.add(lbl("Email"),        0, r); g.add(tfEmail,   1, r++);
        g.add(lbl("Age"),          0, r); g.add(spAge,     1, r++);
        g.add(lbl("ID Type *"),    0, r); g.add(cbIdType,  1, r++);
        g.add(lbl("ID Number *"),  0, r); g.add(tfIdNum,   1, r++);
        g.add(lbl("Address"),      0, r); g.add(tfAddress, 1, r++);

        card.getChildren().add(g);
        return card;
    }

    // ── Room / stay card ─────────────────────────────────────────────────────
    private VBox buildRoomCard() {
        VBox card = card("Room & Stay Details");
        GridPane g = grid();

        cbRoom = new ComboBox<>();
        cbRoom.setStyle(UIStyles.COMBO);
        cbRoom.setMaxWidth(Double.MAX_VALUE);
        cbRoom.setPlaceholder(new Label("No rooms available"));
        cbRoom.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null :
                    "Room " + r.getRoomNumber() + "  |  " +
                    r.getRoomType().getDisplayName() + "  |  Floor " +
                    r.getFloor() + "  |  Rs " +
                    String.format("%,.0f", r.getPricePerNight()) + "/night");
            }
        });
        cbRoom.setButtonCell(cbRoom.getCellFactory().call(null));
        cbRoom.setOnAction(e -> updateBill());

        Button btnRefresh = new Button("Refresh Available Rooms");
        btnRefresh.setStyle(UIStyles.BTN_OUTLINE);
        btnRefresh.setOnMouseEntered(e -> btnRefresh.setStyle(UIStyles.BTN_OUTLINE_H));
        btnRefresh.setOnMouseExited(e  -> btnRefresh.setStyle(UIStyles.BTN_OUTLINE));
        btnRefresh.setOnAction(e -> loadRooms());

        dpIn  = new DatePicker(LocalDate.now());
        dpIn.setStyle(UIStyles.COMBO);          // COMBO == DATE_PICKER alias
        dpIn.setMaxWidth(Double.MAX_VALUE);
        dpIn.setOnAction(e -> updateBill());

        dpOut = new DatePicker(LocalDate.now().plusDays(1));
        dpOut.setStyle(UIStyles.COMBO);
        dpOut.setMaxWidth(Double.MAX_VALUE);
        dpOut.setOnAction(e -> updateBill());

        taSpecial = new TextArea();
        taSpecial.setPromptText("Special requests or notes  (optional)");
        taSpecial.setPrefRowCount(3);
        taSpecial.setStyle("-fx-font-size:13px;-fx-border-color:#BDC3C7;-fx-border-radius:3;");

        int r = 0;
        g.add(lbl("Available Room *"), 0, r); g.add(cbRoom,      1, r++);
        g.add(new Label(),             0, r); g.add(btnRefresh,  1, r++);
        g.add(lbl("Check-In Date *"),  0, r); g.add(dpIn,        1, r++);
        g.add(lbl("Check-Out Date *"), 0, r); g.add(dpOut,       1, r++);
        g.add(lbl("Special Requests"), 0, r); g.add(taSpecial,   1, r++);

        card.getChildren().add(g);
        return card;
    }

    // ── Services card ─────────────────────────────────────────────────────────
    private VBox buildServicesCard() {
        VBox card = card("Additional Services");

        Label note = new Label(
            "Select services to add to the bill.\n" +
            "Charges are per stay and include in final amount.");
        note.setStyle("-fx-font-size:11px;-fx-text-fill:#717D7E;-fx-wrap-text:true;");
        note.setMaxWidth(280);

        chkCleaning = chk("Room Cleaning",  "Rs 300");
        chkLaundry  = chk("Laundry",        "Rs 500");
        chkExtraBed = chk("Extra Bed",       "Rs 800");
        chkMiniBar  = chk("Mini-bar",        "Rs 1,200");

        for (CheckBox cb : List.of(chkCleaning, chkLaundry, chkExtraBed, chkMiniBar))
            cb.setOnAction(e -> updateBill());

        card.getChildren().addAll(note, chkCleaning, chkLaundry, chkExtraBed, chkMiniBar);
        return card;
    }

    // ── Bill preview card ─────────────────────────────────────────────────────
    private VBox buildBillCard() {
        VBox card = card("Bill Preview");

        billPlaceholder = new Label("Select a room to see the estimated bill.");
        billPlaceholder.setStyle("-fx-font-size:12px;-fx-text-fill:#717D7E;-fx-wrap-text:true;");

        lblRoomCharge = billVal();
        lblSvcCharge  = billVal();
        lblGst        = billVal();
        lblTotal      = new Label("Rs 0.00");
        lblTotal.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:#1A5276;");

        GridPane bg = new GridPane();
        bg.setHgap(8); bg.setVgap(9);
        bg.add(bk("Room Charge:"),  0, 0); bg.add(lblRoomCharge, 1, 0);
        bg.add(bk("Services:"),     0, 1); bg.add(lblSvcCharge,  1, 1);
        bg.add(bk("GST (18%):"),    0, 2); bg.add(lblGst,        1, 2);
        bg.add(new Separator(),     0, 3, 2, 1);
        bg.add(bk("TOTAL:"),        0, 4); bg.add(lblTotal,      1, 4);

        billDetailsBox = new VBox(10, bg);
        billDetailsBox.setVisible(false);
        billDetailsBox.setManaged(false);

        card.getChildren().addAll(billPlaceholder, billDetailsBox);
        return card;
    }

    // ── Action buttons ────────────────────────────────────────────────────────
    private VBox buildButtons() {
        Button btnConfirm = new Button("Confirm Booking");
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        btnConfirm.setPrefHeight(44);
        btnConfirm.setStyle(UIStyles.BTN_SUCCESS + "-fx-font-size:14px;");
        btnConfirm.setOnMouseEntered(e -> btnConfirm.setStyle(UIStyles.BTN_SUCCESS_H + "-fx-font-size:14px;"));
        btnConfirm.setOnMouseExited(e  -> btnConfirm.setStyle(UIStyles.BTN_SUCCESS   + "-fx-font-size:14px;"));
        btnConfirm.setOnAction(e -> handleConfirm());

        Button btnClear = new Button("Clear Form");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setStyle(UIStyles.BTN_OUTLINE);
        btnClear.setOnMouseEntered(e -> btnClear.setStyle(UIStyles.BTN_OUTLINE_H));
        btnClear.setOnMouseExited(e  -> btnClear.setStyle(UIStyles.BTN_OUTLINE));
        btnClear.setOnAction(e -> clearForm());

        return new VBox(10, btnConfirm, btnClear);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Logic
    // ══════════════════════════════════════════════════════════════════════════

    private void loadRooms() {
        cbRoom.setItems(FXCollections.observableArrayList(svc.getAvailableRooms()));
        if (!cbRoom.getItems().isEmpty()) cbRoom.getSelectionModel().selectFirst();
        updateBill();
    }

    private void updateBill() {
        Room room = cbRoom.getValue();
        LocalDate ci = dpIn.getValue(), co = dpOut.getValue();

        if (room == null || ci == null || co == null || !co.isAfter(ci)) {
            // Hide bill, show placeholder
            billDetailsBox.setVisible(false);
            billDetailsBox.setManaged(false);
            billPlaceholder.setVisible(true);
            billPlaceholder.setManaged(true);
            return;
        }

        // Show bill, hide placeholder
        billPlaceholder.setVisible(false);
        billPlaceholder.setManaged(false);
        billDetailsBox.setVisible(true);
        billDetailsBox.setManaged(true);

        long   nights  = ChronoUnit.DAYS.between(ci, co);
        double roomCh  = room.getPricePerNight() * nights;
        double svcCh   = selectedServices().stream().mapToDouble(ServiceType::getCharge).sum();
        double gst     = (roomCh + svcCh) * 0.18;
        double total   = roomCh + svcCh + gst;

        lblRoomCharge.setText(String.format("Rs %,.0f  x  %d night%s",
                room.getPricePerNight(), nights, nights == 1 ? "" : "s"));
        lblSvcCharge.setText(String.format("Rs %,.2f", svcCh));
        lblGst.setText(String.format("Rs %,.2f", gst));
        lblTotal.setText(String.format("Rs %,.2f", total));
    }

    private void handleConfirm() {
        if (tfName.getText().isBlank())  { warn("Guest name is required.");    return; }
        if (tfPhone.getText().isBlank()) { warn("Phone number is required.");  return; }
        if (cbIdType.getValue() == null) { warn("Please select an ID type."); return; }
        if (tfIdNum.getText().isBlank()) { warn("ID number is required.");     return; }
        if (cbRoom.getValue() == null)   { warn("Please select a room.");      return; }

        LocalDate ci = dpIn.getValue(), co = dpOut.getValue();
        if (ci == null || co == null || !co.isAfter(ci)) {
            warn("Check-out date must be after check-in date."); return;
        }

        String summary = String.format(
            "Guest : %s\nRoom  : Room %s — %s\nDates : %s  to  %s  (%d nights)\nTotal : %s\n\nConfirm this booking?",
            tfName.getText().trim(),
            cbRoom.getValue().getRoomNumber(), cbRoom.getValue().getRoomType().getDisplayName(),
            ci, co, ChronoUnit.DAYS.between(ci, co),
            lblTotal.getText());

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Confirm Booking");
        conf.setHeaderText("Create Reservation");
        conf.setContentText(summary);
        conf.getDialogPane().setStyle("-fx-font-size:13px;");
        conf.getDialogPane().setPrefWidth(420);
        conf.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> doBook());
    }

    private void doBook() {
        Guest guest = new Guest(
            tfName.getText().trim(), tfPhone.getText().trim(),
            tfEmail.getText().trim(), cbIdType.getValue(),
            tfIdNum.getText().trim(), spAge.getValue(),
            tfAddress.getText().trim()
        );

        HotelService.BookingResult res = svc.bookRoom(
            guest, cbRoom.getValue(),
            dpIn.getValue(), dpOut.getValue(),
            selectedServices(), taSpecial.getText()
        );

        if (res.success) {
            AutoSaveService.getInstance().sendNotification(
                "Booking confirmed: " + res.booking.getBookingId());
            showReceipt(res.booking);
            clearForm();
            if (onBookingMade != null) onBookingMade.run();
        } else {
            warn(res.message);
            loadRooms();
        }
    }

    private void showReceipt(Booking booking) {
        String text = ReceiptGenerator.generateReceipt(booking);

        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Booking Confirmed — Receipt");
        DialogPane dp = dlg.getDialogPane();
        dp.setStyle("-fx-background-color:white;");
        dp.setPrefWidth(520);
        dp.getButtonTypes().add(ButtonType.CLOSE);
        dp.lookupButton(ButtonType.CLOSE).setStyle(UIStyles.BTN_PRIMARY);

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));

        Label ok = new Label("Booking Confirmed!");
        ok.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1E8449;");

        TextArea ta = new TextArea(text);
        ta.setEditable(false);
        ta.setPrefHeight(420);
        ta.setStyle(
            "-fx-font-family:'Courier New',monospace;" +
            "-fx-font-size:12px;" +
            "-fx-background-color:#F8F9FA;" +
            "-fx-border-color:#D5D8DC;");

        Label saved = new Label("Receipt saved to: " +
            System.getProperty("user.home") + "/SilverviewHMS/receipts/");
        saved.setStyle("-fx-font-size:11px;-fx-text-fill:#717D7E;");

        box.getChildren().addAll(ok, ta, saved);
        dp.setContent(box);
        dlg.showAndWait();
    }

    private void clearForm() {
        tfName.clear(); tfPhone.clear(); tfEmail.clear();
        tfAddress.clear(); tfIdNum.clear();
        cbIdType.setValue(null);
        spAge.getValueFactory().setValue(25);
        taSpecial.clear();
        chkCleaning.setSelected(false); chkLaundry.setSelected(false);
        chkExtraBed.setSelected(false); chkMiniBar.setSelected(false);
        dpIn.setValue(LocalDate.now());
        dpOut.setValue(LocalDate.now().plusDays(1));
        loadRooms();
    }

    private List<ServiceType> selectedServices() {
        List<ServiceType> list = new ArrayList<>();
        if (chkCleaning.isSelected()) list.add(ServiceType.ROOM_CLEANING);
        if (chkLaundry.isSelected())  list.add(ServiceType.LAUNDRY);
        if (chkExtraBed.isSelected()) list.add(ServiceType.EXTRA_BED);
        if (chkMiniBar.isSelected())  list.add(ServiceType.MINI_BAR);
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Widget helpers — all inline, no dependency on removed UIStyles constants
    // ══════════════════════════════════════════════════════════════════════════

    /** Card container with heading + separator */
    private VBox card(String heading) {
        VBox c = new VBox(12);
        c.setPadding(new Insets(18));
        c.setStyle(UIStyles.CARD);
        Label h = new Label(heading);
        h.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1C2833;");
        c.getChildren().addAll(h, new Separator());
        return c;
    }

    /** Two-column form grid: label col (130 px) + expanding field col */
    private GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(14); g.setVgap(11);
        ColumnConstraints c0 = new ColumnConstraints(130);
        c0.setHalignment(javafx.geometry.HPos.RIGHT);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c0, c1);
        return g;
    }

    /** Full-width rectangular text field */
    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(UIStyles.FIELD);
        tf.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(tf, true);
        return tf;
    }

    /** Form label (right-aligned) */
    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px;-fx-text-fill:#566573;");
        l.setAlignment(Pos.CENTER_RIGHT);
        return l;
    }

    /** Service checkbox */
    private CheckBox chk(String name, String price) {
        CheckBox cb = new CheckBox(name + "   (" + price + ")");
        cb.setStyle("-fx-font-size:13px;-fx-text-fill:#1C2833;");
        return cb;
    }

    /** Bill value label */
    private Label billVal() {
        Label l = new Label("Rs 0.00");
        l.setStyle("-fx-font-size:13px;-fx-text-fill:#1C2833;");
        return l;
    }

    /** Bill key label */
    private Label bk(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px;-fx-text-fill:#717D7E;-fx-min-width:110;");
        return l;
    }

    /** Warning alert */
    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Validation"); a.setHeaderText(null); a.setContentText(msg);
        a.getDialogPane().setStyle("-fx-font-size:13px;");
        a.showAndWait();
    }

    public void refresh() { loadRooms(); }
}
