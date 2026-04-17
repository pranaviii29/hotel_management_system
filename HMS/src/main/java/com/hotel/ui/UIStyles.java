package com.hotel.ui;

public class UIStyles {

    // ── Cards ─────────────────────────────────────────────────────────────────
    public static final String CARD =
        "-fx-background-color:white;" +
        "-fx-border-color:#D5D8DC;" +
        "-fx-border-radius:5;" +
        "-fx-background-radius:5;" +
        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);";

    public static final String FORM_CARD = CARD;

    // ── Input fields ──────────────────────────────────────────────────────────
    public static final String FIELD =
        "-fx-background-color:white;" +
        "-fx-border-color:#BDC3C7;" +
        "-fx-border-width:1;" +
        "-fx-border-radius:3;" +
        "-fx-background-radius:3;" +
        "-fx-padding:7 10 7 10;" +
        "-fx-font-size:13px;" +
        "-fx-text-fill:#1C2833;" +
        "-fx-pref-height:34;";

    public static final String FIELD_FOCUS =
        "-fx-background-color:white;" +
        "-fx-border-color:#2471A3;" +
        "-fx-border-width:1.5;" +
        "-fx-border-radius:3;" +
        "-fx-background-radius:3;" +
        "-fx-padding:7 10 7 10;" +
        "-fx-font-size:13px;" +
        "-fx-text-fill:#1C2833;" +
        "-fx-pref-height:34;";

    public static final String COMBO =
        "-fx-background-color:white;" +
        "-fx-border-color:#BDC3C7;" +
        "-fx-border-width:1;" +
        "-fx-border-radius:3;" +
        "-fx-background-radius:3;" +
        "-fx-font-size:13px;" +
        "-fx-pref-height:34;";

    public static final String SPINNER     = COMBO;
    public static final String DATE_PICKER = COMBO;

    // ── Labels ────────────────────────────────────────────────────────────────
    public static final String LABEL_FORM =
        "-fx-font-size:12px;" +
        "-fx-text-fill:#566573;";

    // ── Buttons ───────────────────────────────────────────────────────────────
    public static final String BTN_PRIMARY =
        "-fx-background-color:#1E2A38;-fx-text-fill:white;-fx-font-size:13px;" +
        "-fx-font-weight:bold;-fx-padding:9 24;-fx-background-radius:4;-fx-cursor:hand;";

    public static final String BTN_PRIMARY_H =
        "-fx-background-color:#17202A;-fx-text-fill:white;-fx-font-size:13px;" +
        "-fx-font-weight:bold;-fx-padding:9 24;-fx-background-radius:4;-fx-cursor:hand;";

    public static final String BTN_PRIMARY_HOVER = BTN_PRIMARY_H;

    public static final String BTN_SUCCESS =
        "-fx-background-color:#1E8449;-fx-text-fill:white;-fx-font-size:13px;" +
        "-fx-font-weight:bold;-fx-padding:9 24;-fx-background-radius:4;-fx-cursor:hand;";

    public static final String BTN_SUCCESS_H =
        "-fx-background-color:#196F3D;-fx-text-fill:white;-fx-font-size:13px;" +
        "-fx-font-weight:bold;-fx-padding:9 24;-fx-background-radius:4;-fx-cursor:hand;";

    public static final String BTN_SUCCESS_HOVER = BTN_SUCCESS_H;

    public static final String BTN_DANGER =
        "-fx-background-color:#922B21;-fx-text-fill:white;-fx-font-size:12px;" +
        "-fx-font-weight:bold;-fx-padding:6 14;-fx-background-radius:4;-fx-cursor:hand;";

    public static final String BTN_DANGER_H =
        "-fx-background-color:#7B241C;-fx-text-fill:white;-fx-font-size:12px;" +
        "-fx-font-weight:bold;-fx-padding:6 14;-fx-background-radius:4;-fx-cursor:hand;";

    public static final String BTN_OUTLINE =
        "-fx-background-color:transparent;-fx-text-fill:#1E2A38;" +
        "-fx-border-color:#1E2A38;-fx-border-width:1;-fx-border-radius:4;" +
        "-fx-font-size:13px;-fx-padding:8 20;-fx-cursor:hand;";

    public static final String BTN_OUTLINE_H =
        "-fx-background-color:#1E2A38;-fx-text-fill:white;" +
        "-fx-border-color:#1E2A38;-fx-border-width:1;-fx-border-radius:4;" +
        "-fx-font-size:13px;-fx-padding:8 20;-fx-cursor:hand;";

    public static final String BTN_OUTLINE_HOVER = BTN_OUTLINE_H;

    public static final String BTN_SMALL =
        "-fx-background-color:#EBF5FB;-fx-text-fill:#1E2A38;" +
        "-fx-font-size:11px;-fx-padding:5 12;-fx-background-radius:3;-fx-cursor:hand;";

    // ── Table ─────────────────────────────────────────────────────────────────
    public static final String TABLE =
        "-fx-background-color:white;-fx-border-color:#D5D8DC;" +
        "-fx-border-radius:5;-fx-background-radius:5;";

    // ── Status badges ─────────────────────────────────────────────────────────
    public static String badge(String status) {
        return switch (status) {
            case "Available"     ->
                "-fx-background-color:#D5F5E3;-fx-text-fill:#1E8449;" +
                "-fx-background-radius:3;-fx-padding:2 8;-fx-font-size:11px;-fx-font-weight:bold;";
            case "Occupied"      ->
                "-fx-background-color:#FADBD8;-fx-text-fill:#922B21;" +
                "-fx-background-radius:3;-fx-padding:2 8;-fx-font-size:11px;-fx-font-weight:bold;";
            case "Confirmed"     ->
                "-fx-background-color:#D6EAF8;-fx-text-fill:#1A5276;" +
                "-fx-background-radius:3;-fx-padding:2 8;-fx-font-size:11px;-fx-font-weight:bold;";
            case "Checked Out"   ->
                "-fx-background-color:#E8DAEF;-fx-text-fill:#6C3483;" +
                "-fx-background-radius:3;-fx-padding:2 8;-fx-font-size:11px;-fx-font-weight:bold;";
            case "Completed"     ->
                "-fx-background-color:#D5F5E3;-fx-text-fill:#1E8449;" +
                "-fx-background-radius:3;-fx-padding:2 8;-fx-font-size:11px;-fx-font-weight:bold;";
            case "Pending"       ->
                "-fx-background-color:#FDEBD0;-fx-text-fill:#A04000;" +
                "-fx-background-radius:3;-fx-padding:2 8;-fx-font-size:11px;-fx-font-weight:bold;";
            case "Not Completed" ->
                "-fx-background-color:#FADBD8;-fx-text-fill:#922B21;" +
                "-fx-background-radius:3;-fx-padding:2 8;-fx-font-size:11px;-fx-font-weight:bold;";
            default              ->
                "-fx-background-color:#F2F3F4;-fx-text-fill:#566573;" +
                "-fx-background-radius:3;-fx-padding:2 8;-fx-font-size:11px;";
        };
    }
}
