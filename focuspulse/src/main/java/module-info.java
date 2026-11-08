module com.focuspulse {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.focuspulse to javafx.fxml;
    exports com.focuspulse;
}
