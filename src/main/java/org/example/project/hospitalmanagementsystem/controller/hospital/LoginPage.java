package org.example.project.hospitalmanagementsystem.controller.hospital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import org.example.project.hospitalmanagementsystem.controller.admin.Admin;
import org.example.project.hospitalmanagementsystem.controller.users.Complaint;
import org.example.project.hospitalmanagementsystem.controller.users.homepage;
import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPage {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordFieldVisible;

    @FXML
    private ChoiceBox<String> roleChoiceBox;

    @FXML
    private Hyperlink registerHereLink;

    @FXML
    private Label errorMessage;

    @FXML
    private Button loginButton;

    @FXML
    private Button showPwd;

    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        roleChoiceBox.getItems().addAll("Patient", "Admin");
        roleChoiceBox.setValue("Patient");
        bindPasswordFields();
    }

    private void bindPasswordFields() {
        passwordFieldVisible.managedProperty().bind(passwordFieldVisible.visibleProperty());
        passwordField.managedProperty().bind(passwordField.visibleProperty());
        passwordFieldVisible.setVisible(false);
    }

    @FXML
    private void togglePassword(ActionEvent e) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordFieldVisible.setText(passwordField.getText());
            passwordFieldVisible.setVisible(true);
            passwordField.setVisible(false);
            showPwd.setText("👁‍🗨");
        } else {
            passwordField.setText(passwordFieldVisible.getText());
            passwordField.setVisible(true);
            passwordFieldVisible.setVisible(false);
            showPwd.setText("👁");
        }
    }

    @FXML
    private void handleLoginSubmit(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordVisible ? passwordFieldVisible.getText() : passwordField.getText();
        String role = roleChoiceBox.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null) {
            showError("Please fill in all fields.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE email = ? AND role = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, role);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");

                if (storedPassword.equals(password)) {
                    System.out.println("Login successful!");

                    String name = resultSet.getString("name"); // ← Get user's real name
                    String userEmail = resultSet.getString("email");

                    FXMLLoader loader  ;
                    if ("Admin".equals(role)) {
                        loader = new FXMLLoader(getClass().getResource("/fxml/admin/adminpage.fxml"));
                    } else {
                        loader = new FXMLLoader(getClass().getResource("/fxml/user/Homepage.fxml"));
                    }
                    Parent root = loader.load();
                    if ("Admin".equals(role)) {
                        Admin controller = loader.getController();
                        controller.setAdminNameLabel(name); // ← Use name
                    } else {
                        homepage controller = loader.getController();
                        controller.setUsername(name); // ← Use name
                        controller.setUserEmail(userEmail); // NEW: Pass user ID

                        Complaint complaintController = controller.getComplaintController(); // <-- You must expose this method in homepage
                        if (complaintController != null) {
                            complaintController.setUserEmail(userEmail); // ✅ Pass user ID to complaint controller
                        }

                    }

                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());

                    stage.setScene(scene);
                    stage.setTitle("Trumedix - Home");
                    stage.setResizable(true);
                    stage.setMaximized(true);
                    stage.show();

                } else {
                    showError("Incorrect password.");
                }
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showError("Database or loading error.");
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        showInfo("Forgot Password", "Contact admin or check your email for reset instructions.");
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hospital/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerHereLink.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle("Register");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load register page");
        }
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initModality(Modality.NONE);

        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("/media/Generate.png")));

        alert.show();
    }
}