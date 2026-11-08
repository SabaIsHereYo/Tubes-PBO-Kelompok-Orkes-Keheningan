module com.focuspulse {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.focuspulse to javafx.fxml;
    opens com.focuspulse.controllers to javafx.fxml;
    exports com.focuspulse;
}