package org.example.project.hospitalmanagementsystem.controller.users;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.example.project.hospitalmanagementsystem.controller.admin.AppointmentDAO;
import org.example.project.hospitalmanagementsystem.database.DatabaseHandler;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Appointment {

    @FXML private TextField        nameField;
    @FXML private TextField        emailField;
    @FXML private ComboBox<String> departmentComboBox;
    @FXML private ComboBox<String> doctorComboBox;
    @FXML private DatePicker       datePicker;
    @FXML private TextArea         notesArea;
    @FXML private Label            errorLabel;
    @FXML private ComboBox<String> timeComboBox;

    private final Map<String, List<String>> departmentDoctors = new HashMap<>();

    @FXML
    public void initialize() {
        departmentComboBox.getItems().addAll(
                "Cardiology", "Neurology", "Pediatrics",
                "Dermatology", "Clinical(Check-ups)", "Maternity");

        departmentDoctors.put("Cardiology",         List.of("Dr. Smith", "Dr. Adams Lincoln"));
        departmentDoctors.put("Neurology",          List.of("Dr. Brown Antwi", "Dr. Lena Kwarteng"));
        departmentDoctors.put("Pediatrics",         List.of("Dr. Samantha", "Dr. Viola Davis"));
        departmentDoctors.put("Dermatology",        List.of("Dr. Rose", "Dr. Marcus Foley"));
        departmentDoctors.put("Clinical(Check-ups)",List.of("Dr. Alice", "Dr. Bob"));
        departmentDoctors.put("Maternity",          List.of("Dr. Stephanie", "Dr. Maxine Harding"));

        departmentComboBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> handleDepartmentSelection());

        timeComboBox.getItems().addAll(
                "09:00 AM", "10:00 AM", "11:00 AM",
                "12:00 PM", "01:00 PM", "02:00 PM",
                "03:00 PM", "04:00 PM", "05:00 PM");

        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            emailField.setText(session.getUserEmail());
            nameField.setText(session.getUserName());
        }
    }

    @FXML
    private void handleDepartmentSelection() {
        String dept = departmentComboBox.getValue();
        doctorComboBox.getItems().clear();
        if (dept != null && departmentDoctors.containsKey(dept)) {
            List<String> docs = departmentDoctors.get(dept);
            doctorComboBox.getItems().addAll(docs);
            if (!docs.isEmpty()) doctorComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        String name       = nameField.getText().trim();
        String email      = emailField.getText().trim();
        String department = departmentComboBox.getValue();
        String doctor     = doctorComboBox.getValue();
        String time       = timeComboBox.getValue();
        LocalDate localDate = datePicker.getValue();

        if (name.isEmpty() || email.isEmpty() || department == null || doctor == null || localDate == null || time == null) {
            showError("Please fill out all fields.");
            return;
        }
        if (!isValidEmail(email)) { showError("Invalid email format."); return; }

        Integer userId   = DatabaseHandler.getUserIdByEmail(email);
        if (userId == null) { showError("User not registered."); return; }

        Integer doctorId = DatabaseHandler.getdoctorIdByName(doctor);
        if (doctorId == null) { showError("Doctor doesn't exist."); return; }

        boolean success = AppointmentDAO.insertAppointment(
                userId, doctorId, name, email, department, doctor,
                Date.valueOf(localDate), time, notesArea.getText().trim());

        if (success) {
            showError("Appointment booked successfully.", Color.GREEN);
            clearForm();
        } else {
            showError("Failed to book appointment. Try again.");
        }
    }

    private void showError(String msg)              { showError(msg, Color.RED); }
    private void showError(String msg, Color color) {
        errorLabel.setTextFill(color);
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        departmentComboBox.getSelectionModel().clearSelection();
        doctorComboBox.getItems().clear();
        timeComboBox.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        notesArea.clear();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    @FXML
    private void handleBackToHome(ActionEvent event) {
        NavHelper.goHome((Node) event.getSource());
    }
}