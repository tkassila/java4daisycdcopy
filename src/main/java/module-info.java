module com.metait.java4daisycdcopy {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.metait.java4daisycdcopy to javafx.fxml;
    exports com.metait.java4daisycdcopy;
}