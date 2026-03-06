package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class EditPatientController {

    @FXML private TextField  nameField;
    @FXML private TextField  emailField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private DatePicker dobPicker;
    @FXML private TextArea   addressArea;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label      errorLabel;

    private ManagePatientsController.Patient patient;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        genderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        statusCombo.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        errorLabel.setText("");
    }

    public void setPatient(ManagePatientsController.Patient p) {
        this.patient = p;

        nameField.setText(p.getName());
        emailField.setText(p.getEmail());
        genderCombo.setValue(p.getGender());
        addressArea.setText(p.getAddress());
        statusCombo.setValue(p.getStatus());
        try {
            dobPicker.setValue(LocalDate.parse(p.getDob()));
        } catch (DateTimeParseException ex) {
            dobPicker.setValue(null);
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        String gender  = genderCombo.getValue();
        String address = addressArea.getText().trim();
        String status  = statusCombo.getValue();
        LocalDate dob  = dobPicker.getValue();

        if (name.isEmpty()) { errorLabel.setText("Name is required."); return; }
        if (email.isEmpty() || !email.contains("@")) { errorLabel.setText("A valid email is required."); return; }
        if (gender == null)  { errorLabel.setText("Please select a gender."); return; }
        if (dob == null)     { errorLabel.setText("Please select a date of birth."); return; }
        if (status == null)  { errorLabel.setText("Please select a status."); return; }


        String sql = """
                UPDATE users
                SET name = ?, email = ?, gender = ?, birth_date = ?, address = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, gender);
            ps.setDate(4, java.sql.Date.valueOf(dob));
            ps.setString(5, address);
            ps.setInt(6, patient.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Database error: " + e.getMessage());
            return;
        }

        patient.nameProperty().set(name);
        patient.emailProperty().set(email);
        patient.genderProperty().set(gender);
        patient.dobProperty().set(dob.toString());
        patient.addressProperty().set(address);
        patient.setStatus(status);
        if (onSaveCallback != null) onSaveCallback.run();

        closeWindow();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}