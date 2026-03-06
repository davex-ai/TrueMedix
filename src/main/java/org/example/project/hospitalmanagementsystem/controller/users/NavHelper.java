package org.example.project.hospitalmanagementsystem.controller.users;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavHelper {

    private NavHelper() {}

    public static void goTo(String fxmlPath, String title, Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(NavHelper.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle(title);
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void goHome(Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(NavHelper.class.getResource("/fxml/user/Homepage.fxml"));
            Parent root = loader.load();

            homepage ctrl = loader.getController();
            UserSession s = UserSession.getInstance();
            if (s.isLoggedIn()) {
                ctrl.setUsername(s.getUserName());
                ctrl.setUserEmail(s.getUserEmail());
            }

            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle("Trumedix - Home");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}