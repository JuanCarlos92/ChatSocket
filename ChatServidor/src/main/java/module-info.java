module org.example.chatservidor {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.chatservidor to javafx.fxml;
    opens org.example.chatservidor.controller to javafx.fxml;
    exports org.example.chatservidor;
}