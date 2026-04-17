package com.hotel.ui;

import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.service.HotelService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Arrays;
import java.util.List;

public class RoomsPanel extends VBox {

    private final HotelService svc;
    private TableView<Room> table;
    private ObservableList<Room> data;
    private TextField tfSearch;
    private ComboBox<String> cbType;
    private CheckBox chkAvail;

    public RoomsPanel() {
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

        // header
        HBox hdr = new HBox();
        hdr.setAlignment(Pos.CENTER_LEFT);
        VBox tbox = new VBox(3);
        Label title = new Label("Room Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1C2833;");
        Label sub = new Label("Manage all hotel rooms and their availability status");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #717D7E;");
        tbox.getChildren().addAll(title, sub);
        HBox.setHgrow(tbox, Priority.ALWAYS);
        Button btnAdd = btn("+ Add Room", UIStyles.BTN_PRIMARY, UIStyles.BTN_PRIMARY_H);
        btnAdd.setOnAction(e -> showAddDialog());
        hdr.getChildren().addAll(tbox, btnAdd);

        // filter bar
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12 ,16, 12, 16));
        bar.setStyle(UIStyles.CARD);

        tfSearch = new TextField();
        tfSearch.setPromptText("Search room, floor, type...");
        tfSearch.setStyle(UIStyles.FIELD);
        tfSearch.setPrefWidth(240);
        tfSearch.textProperty().addListener((o, ov, nv) -> filter());

        cbType = new ComboBox<>();
        cbType.getItems().add("All Types");
        Arrays.stream(RoomType.values()).forEach(rt -> cbType.getItems().add(rt.getDisplayName()));
        cbType.setValue("All Types");
        cbType.setStyle(UIStyles.COMBO); cbType.setPrefWidth(140);
        cbType.setOnAction(e -> filter());

        chkAvail = new CheckBox("Available Only");
        chkAvail.setStyle("-fx-font-size: 13px;");
        chkAvail.setOnAction(e -> filter());

        Button btnRef = btn("Refresh", UIStyles.BTN_OUTLINE, UIStyles.BTN_OUTLINE_H);
        btnRef.setOnAction(e -> refresh());

        bar.getChildren().addAll(
            new Label("Search:"), tfSearch,
            new Label("Type:"), cbType, chkAvail,
            new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
            btnRef
        );

        // table
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
    private TableView<Room> buildTable() {
        TableView<Room> t = new TableView<>();
        t.setStyle(UIStyles.TABLE);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPrefHeight(520);
        t.setPlaceholder(new Label("No rooms found."));

        t.getColumns().addAll(
            col("Room No.",   d -> d.getRoomNumber(),                              90),
            col("Type",       d -> d.getRoomType().getDisplayName(),              130),
            col("Floor",      d -> d.getFloor(),                                   70),
            col("Capacity",   d -> d.getCapacity() + " guests",                    90),
            col("Rate/Night", d -> "Rs " + String.format("%,.0f", d.getPricePerNight()), 110),
            statusCol(),
            col("Amenities",  d -> d.getAmenitiesString(),                        230)
        );

        data = FXCollections.observableArrayList();
        t.setItems(data);
        return t;
    }

    private TableColumn<Room, String> col(String name, java.util.function.Function<Room, String> fn, double w) {
        TableColumn<Room, String> c = new TableColumn<>(name);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w);
        return c;
    }

    private TableColumn<Room, String> statusCol() {
        TableColumn<Room, String> c = new TableColumn<>("Status");
        c.setPrefWidth(100);
        c.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatusDisplay()));
        c.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label l = new Label(item);
                l.setStyle(UIStyles.badge(item));
                setGraphic(l); setText(null);
            }
        });
        return c;
    }

    private void filter() {
        String q = tfSearch.getText();
        RoomType rt = null;
        if (cbType.getValue() != null && !cbType.getValue().equals("All Types"))
            for (RoomType r : RoomType.values())
                if (r.getDisplayName().equals(cbType.getValue())) { rt = r; break; }
        data.setAll(svc.searchRooms(q, rt, chkAvail.isSelected()));
    }

    private void showAddDialog() {
        Dialog<Room> dlg = new Dialog<>();
        dlg.setTitle("Add New Room");
        DialogPane dp = dlg.getDialogPane();
        dp.setStyle("-fx-background-color: white; -fx-font-size: 13px;");
        dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dp.lookupButton(ButtonType.OK).setStyle(UIStyles.BTN_PRIMARY);
        dp.lookupButton(ButtonType.CANCEL).setStyle(UIStyles.BTN_OUTLINE);

        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(11); g.setPadding(new Insets(20));

        TextField tfNum   = fld("e.g. 105");
        TextField tfFloor = fld("e.g. 1");
        TextField tfPrice = fld("Leave blank for default");
        ComboBox<RoomType> cbT = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        cbT.setPromptText("Select type"); cbT.setStyle(UIStyles.COMBO); cbT.setMaxWidth(Double.MAX_VALUE);
        cbT.setOnAction(e -> { if (cbT.getValue() != null) tfPrice.setPromptText("Default: Rs " + String.format("%,.0f", cbT.getValue().getBasePrice())); });
        Spinner<Integer> spCap = new Spinner<>(1, 10, 2); spCap.setEditable(true);

        g.add(new Label("Room Number:"), 0, 0); g.add(tfNum,   1, 0);
        g.add(new Label("Room Type:"),  0, 1); g.add(cbT,    1, 1);
        g.add(new Label("Floor:"),      0, 2); g.add(tfFloor,1, 2);
        g.add(new Label("Capacity:"),   0, 3); g.add(spCap,  1, 3);
        g.add(new Label("Price/Night:"),0, 4); g.add(tfPrice,1, 4);
        dp.setContent(g);

        dp.lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (tfNum.getText().isBlank() || cbT.getValue() == null || tfFloor.getText().isBlank()) {
                Alert a = new Alert(Alert.AlertType.ERROR); a.setHeaderText(null);
                a.setContentText("Fill all required fields."); a.showAndWait();
                ev.consume(); return;
            }
            boolean dup = svc.getAllRooms().stream().anyMatch(r -> r.getRoomNumber().equals(tfNum.getText().trim()));
            if (dup) {
                Alert a = new Alert(Alert.AlertType.ERROR); a.setHeaderText(null);
                a.setContentText("Room " + tfNum.getText() + " already exists."); a.showAndWait();
                ev.consume();
            }
        });

        dlg.setResultConverter(b -> {
            if (b == ButtonType.OK) {
                Room room = new Room(tfNum.getText().trim(), cbT.getValue(), tfFloor.getText().trim(), spCap.getValue());
                if (!tfPrice.getText().isBlank()) try { room.setPricePerNight(Double.parseDouble(tfPrice.getText())); } catch (Exception ignored) {}
                return room;
            }
            return null;
        });

        dlg.showAndWait().ifPresent(room -> { svc.addRoom(room); refresh(); });
    }

    private Button btn(String text, String s, String sh) {
        Button b = new Button(text); b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(sh));
        b.setOnMouseExited(e  -> b.setStyle(s));
        return b;
    }

    private TextField fld(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt);
        tf.setStyle(UIStyles.FIELD); tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    public void refresh() { data.setAll(svc.getAllRooms()); }
}
