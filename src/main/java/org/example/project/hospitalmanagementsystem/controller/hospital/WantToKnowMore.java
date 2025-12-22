package org.example.project.hospitalmanagementsystem.controller.hospital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

import java.io.IOException;

public class WantToKnowMore {

    @FXML
    private Hyperlink wantToKnowMoreLink;

    @FXML
    private void goBackToWelcome() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hospital/welcome.fxml"));
        Parent root = loader.load();

        WelcomePage controller = loader.getController();
        controller.initialize(); // Explicitly call it (optional but safe if you define it as public)

        Stage stage = (Stage) wantToKnowMoreLink.getScene().getWindow();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }



}
//Video Editing: If you’re into visuals, learn tools like DaVinci
// Resolve (free!) or Adobe Premiere. Offer editing services for
// YouTubers or small businesses.