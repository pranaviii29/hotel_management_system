module com.hotel {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.hotel to javafx.fxml;
    opens com.hotel.model to javafx.fxml;
    opens com.hotel.ui to javafx.fxml;
    opens com.hotel.service to javafx.fxml;
    opens com.hotel.util to javafx.fxml;

    exports com.hotel;
    exports com.hotel.model;
    exports com.hotel.service;
    exports com.hotel.ui;
    exports com.hotel.util;
}
