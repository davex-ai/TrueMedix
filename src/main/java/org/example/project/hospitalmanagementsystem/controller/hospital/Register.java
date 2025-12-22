package org.example.project.hospitalmanagementsystem.controller.hospital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import org.example.project.hospitalmanagementsystem.controller.admin.Admin;
import org.example.project.hospitalmanagementsystem.controller.users.homepage;
import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

import org.example.project.hospitalmanagementsystem.model.Appointment;
import org.example.project.hospitalmanagementsystem.model.Doctor;


public class Register {

    @FXML
    private TextField adminCodeField;

    @FXML
    private HBox adminCodeBox;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordFieldVisible;

    @FXML
    private ChoiceBox<String> roleChoiceBox;

    @FXML
    private ChoiceBox<String> genderchoice;

    @FXML
    private TextField nametext;

    @FXML
    private  TextField addresss;

    @FXML
    private Hyperlink loginHereLink;

    @FXML
    private Label errorMessage;

    @FXML
    private Label messageLabel;

    @FXML
    private DatePicker birth;

    @FXML
    private Button RegisterButton;

    @FXML
    private Button showPwd;

    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        roleChoiceBox.getItems().addAll("Patient", "Admin");
        genderchoice.getItems().addAll("Male", "Female","Other");
        genderchoice.setValue("Gender");
        roleChoiceBox.setValue("Patient");
        bindPasswordFields();

        roleChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAdmin = "Admin".equals(newVal);
            adminCodeBox.setVisible(isAdmin);
            adminCodeBox.setManaged(isAdmin);
        });

        adminCodeBox.setVisible(false);
        adminCodeBox.setManaged(false);
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
    private void handleLogin(ActionEvent event) {
        // Switch to login scene when user clicks "loginHereLink"
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hospital/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginHereLink.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load login page.");
        }
    }

    @FXML
    private void handleRegisterSubmit(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordVisible ? passwordFieldVisible.getText() : passwordField.getText();
        String role = roleChoiceBox.getValue();
        String gender = genderchoice.getValue();
        String name = nametext.getText();
        String address = addresss.getText();
        LocalDate birthdate = birth.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null||gender == null||name.isEmpty()||address.isEmpty() ) {
            showError("Please fill in all fields.");
            return;
        }

        if (role.equals("Admin")) {
            String enteredCode = adminCodeField.getText().trim();
            String correctCode = "SECRET123"; // Better: load this from config/env variable

            if (!correctCode.equals(enteredCode)) {
                showError("Invalid admin registration code.");
                return;
            }
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Check if email already exists
            String checkQuery = "SELECT * FROM users WHERE email = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, email);
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                showError("Email already registered.");
                return;
            }

            // Insert new user
            String insertQuery = "INSERT INTO users (email, password, role, name, gender, address, birth_date) VALUES (?, ?, ?, ?,?, ?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
            insertStmt.setString(1, email);
            insertStmt.setString(2, password);
            insertStmt.setString(3, role);
            insertStmt.setString(4, name);
            insertStmt.setString(5, gender);
            insertStmt.setString(6, address);
            insertStmt.setDate(7, Date.valueOf(birthdate) );

            insertStmt.executeUpdate();

            System.out.println("Registration successful!");

            // Load homepage and pass username/email
            FXMLLoader loader;
            if ("Admin".equals(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/admin/adminpage.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/fxml/user/Homepage.fxml"));
            }
            Parent root = loader.load();

            if ("Admin".equals(role)) {
                Admin controller = loader.getController();
                controller.setAdminNameLabel(name);
            } else {
                homepage controller = loader.getController();
                controller.setUsername(name);
            }


            Stage stage = (Stage) RegisterButton.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle("Trumedix - Home");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showError("Registration failed due to an error.");
        }
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }
}
