module hospital.management.system {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires java.desktop;
    requires mysql.connector.j;

    opens org.example.project.hospitalmanagementsystem.controller to javafx.fxml;

    exports org.example.project.hospitalmanagementsystem.controller;
    exports org.example.project.hospitalmanagementsystem;
    exports org.example.project.hospitalmanagementsystem.controller.admin;
    opens org.example.project.hospitalmanagementsystem.controller.admin to javafx.fxml;
    exports org.example.project.hospitalmanagementsystem.controller.hospital;
    opens org.example.project.hospitalmanagementsystem.controller.hospital to javafx.fxml;
    exports org.example.project.hospitalmanagementsystem.controller.users;
    opens org.example.project.hospitalmanagementsystem.controller.users to javafx.fxml;
}
