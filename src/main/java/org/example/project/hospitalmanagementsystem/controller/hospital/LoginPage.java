package org.example.project.hospitalmanagementsystem.controller.hospital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import org.example.project.hospitalmanagementsystem.controller.admin.Admin;
import org.example.project.hospitalmanagementsystem.controller.users.UserSession;
import org.example.project.hospitalmanagementsystem.controller.users.homepage;
import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;

import java.io.IOException;
import java.sql.*;

public class LoginPage {

    @FXML private TextField        emailField;
    @FXML private PasswordField    passwordField;
    @FXML private TextField        passwordFieldVisible;
    @FXML private ChoiceBox<String> roleChoiceBox;
    @FXML private Hyperlink        registerHereLink;
    @FXML private Label            errorMessage;
    @FXML private Button           loginButton;
    @FXML private Button           showPwd;

    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        roleChoiceBox.getItems().addAll("Patient", "Admin");
        roleChoiceBox.setValue("Patient");
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
        String email    = emailField.getText().trim();
        String password = passwordVisible ? passwordFieldVisible.getText() : passwordField.getText();
        String role     = roleChoiceBox.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null) {
            showError("Please fill in all fields.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND role = ?");
            stmt.setString(1, email);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (!rs.getString("password").equals(password)) {
                    showError("Incorrect password.");
                    return;
                }

                String name      = rs.getString("name");
                String userEmail = rs.getString("email");

                FXMLLoader loader;
                if ("Admin".equals(role)) {
                    loader = new FXMLLoader(getClass().getResource("/fxml/admin/adminpage.fxml"));
                } else {
                    UserSession.getInstance().setUserName(name);
                    UserSession.getInstance().setUserEmail(userEmail);
                    loader = new FXMLLoader(getClass().getResource("/fxml/user/Homepage.fxml"));
                }

                Parent root = loader.load();

                if ("Admin".equals(role)) {
                    Admin ctrl = loader.getController();
                    ctrl.setAdminName(name);
                } else {
                    homepage ctrl = loader.getController();
                    ctrl.setUsername(name);
                    ctrl.setUserEmail(userEmail);
                }

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
                stage.setTitle("Trumedix - Home");
                stage.setResizable(true);
                stage.setMaximized(true);
                stage.show();

            } else {
                showError("No account found with these credentials.");
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
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle("Register");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load register page.");
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
        Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image(getClass().getResourceAsStream("/media/Generate.png")));
        alert.show();
    }
}