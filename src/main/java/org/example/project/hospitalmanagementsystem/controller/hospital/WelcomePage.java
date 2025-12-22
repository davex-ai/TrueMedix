package org.example.project.hospitalmanagementsystem.controller.hospital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

import java.io.IOException;
 import java.net.URL;

public class WelcomePage {

    @FXML
    private MediaView videoView;

    @FXML
    private ImageView hamburgerIcon;

    @FXML
    public void initialize() {
        // Load hamburger icon
        String iconPath = "/media/hamburger.png"; // Ensure this path is correct
        URL iconUrl = getClass().getResource(iconPath);
        if (iconUrl == null) {
            System.err.println("Hamburger icon not found: " + iconPath);
        } else {
            Image hamburgerImage = new Image(iconUrl.toExternalForm());
            hamburgerIcon.setImage(hamburgerImage);
        }

        // Configure video
        String videoPath = "/media/video.mp4";
        URL videoUrl = getClass().getResource(videoPath);
        if (videoUrl == null) {
            System.err.println("Video not found: " + videoPath);
        } else {
            Media media = new Media(videoUrl.toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true); // Mute the video
            videoView.setMediaPlayer(mediaPlayer);
            
        }

    }

    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hospital/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) videoView.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle("Hospital Login");

            // Optional: Make the window resize to fill the screen
            stage.setResizable(true);
            stage.setMaximized(true); // Optional fullscreen
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hospital/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) videoView.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);

            stage.setTitle("Signup Page");
            stage.setResizable(true);
            stage.setMaximized(true); // Important to make it fullscreen
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void WantToKnowMore() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hospital/WantToKnowMore.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) videoView.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);

            stage.setTitle("Want To Know More");
            stage.setResizable(true);
            stage.setMaximized(true); // Important to make it fullscreen
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAboutUs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hospital/AboutUs.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) videoView.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);

            stage.setTitle("About Us");
            stage.setResizable(true);
            stage.setMaximized(true); // Important to make it fullscreen
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
