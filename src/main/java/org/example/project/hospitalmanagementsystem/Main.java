package org.example.project.hospitalmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class Main extends Application {

    public static Scene scene;

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("/fxml/hospital/welcome.fxml"));
            scene = new Scene(root);

            primaryStage.setTitle("TruMedix App");
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/media/Generate.png")));

            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setRoot(String fxmlPath) throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource(fxmlPath));
        scene.setRoot(root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
