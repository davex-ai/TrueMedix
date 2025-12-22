package org.example.project.hospitalmanagementsystem.controller.hospital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

import java.io.IOException;


public class AboutUsController {

    @FXML
    private Hyperlink wantToKnowMoreLink;

    @FXML
    private void goBackToWelcome() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hospital/welcome.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) wantToKnowMoreLink.getScene().getWindow();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight()); // preserve dimensions
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/css/about.css").toExternalForm());
        stage.setTitle("Welcome Page");
        stage.setMaximized(true); // ensure full screen
        stage.show();
    }

}
