package org.example.project.hospitalmanagementsystem.controller.admin;// AddDoctorController.java

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManagePatientsController {
    @FXML
    private TextField searchField;

    @FXML
    private VBox patientsContainer;
    private ResultSet cachedPatients;
    private List<Patient> cachedPatientsList = new ArrayList<>();

    private static class Patient {
        int id;
        String name, email, gender, dob, address, password, role;

        Patient(int id, String name, String password, String email, String gender, String dob, String address, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.gender = gender;
            this.dob = dob;
            this.address = address;
            this.password = password;
            this.role = role;
        }
    }


    @FXML
    public void initialize() {
        loadPatientsFromDatabase();
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterPatients(newVal.trim().toLowerCase());
        });
    }

    private void displayPatients(List<Patient> patients) {
        patientsContainer.getChildren().clear();
        for (Patient p : patients) {
            patientsContainer.getChildren().add(createPatientCard(p));
        }
    }

    private void loadPatientsFromDatabase() {
        String query = "SELECT id, name, email, gender, birth_date, address, password, role FROM users WHERE role = 'patient'";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/trumedixdb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            List<Patient> patientList = new ArrayList<>();
            while (rs.next()) {
                Patient p = new Patient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("gender"),
                        rs.getDate("birth_date").toString(),
                        rs.getString("address"),
                        rs.getString("password"),
                        rs.getString("role")
                );
                patientList.add(p);  // ✅ Correct use

            }
            displayPatients(patientList);
            // Cache this list for filtering
            this.cachedPatientsList = patientList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createPatientCard(Patient p) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 3);");

        Label title = new Label("Patient ID: " + p.id);
        title.setFont(new Font("Arial", 16));
        title.setStyle("-fx-font-weight: bold;");

        Label nameLabel = new Label("Name: " + p.name);
        Label emailLabel = new Label("Email: " + p.password);
        Label genderLabel = new Label("Gender: " + p.email);
        Label dobLabel = new Label("Date of Birth: " + p.gender);
        Label addressLabel = new Label("Address: " + p.dob);
        Label passwordLabel = new Label("password: " + p.address);
        Label roleLabel = new Label("Role: " + p.role);


        card.getChildren().addAll(title, nameLabel, emailLabel, genderLabel, dobLabel, addressLabel, passwordLabel, roleLabel);
        return card;
    }

    private void filterPatients(String query) {
        if (query.isEmpty()) {
            displayPatients(cachedPatientsList);
            return;
        }
        List<Patient> filtered = cachedPatientsList.stream()
                .filter(p -> p.name.toLowerCase().contains(query) || p.email.toLowerCase().contains(query))
                .collect(Collectors.toList());
        displayPatients(filtered);
    }

    @FXML
    private void refreshPatients() {
        loadPatientsFromDatabase();
        searchField.clear();
    }

    @FXML
    private void goBack(ActionEvent event) {
        Stage currentStage = (Stage) patientsContainer.getScene().getWindow();
        currentStage.close();
    }

}

