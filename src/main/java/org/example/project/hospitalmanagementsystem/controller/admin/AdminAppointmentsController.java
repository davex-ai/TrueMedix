package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import org.example.project.hospitalmanagementsystem.model.Appointment;
import org.example.project.hospitalmanagementsystem.database.DatabaseHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AdminAppointmentsController {

    @FXML private TableView<Appointment> appointmentTable;
        @FXML private TableColumn<Appointment, Integer> idColumn;
        @FXML private TableColumn<Appointment, String> nameColumn, emailColumn, departmentColumn, doctorColumn, timeColumn, notesColumn;
        @FXML private TableColumn<Appointment, String> dateColumn, statusColumn;
        @FXML private TextField searchField;
        @FXML public Button approveButton, cancelButton;

    @FXML
    private TextArea messageField;

    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final Map<Integer, String> doctorIdLookup = new HashMap<>();
    private final Map<Integer, String> doctorDepartmentLookup = new HashMap<>();

    @FXML
    public void initialize() {
        preloadDoctorDetails();
        idColumn.setCellValueFactory(cell -> cell.getValue().appointmentIdProperty().asObject());
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        emailColumn.setCellValueFactory(cell -> cell.getValue().emailProperty());

        // These require transformation, because department and doctor name are not in the model
        departmentColumn.setCellValueFactory(cell -> new SimpleStringProperty(getDepartmentNameFromDoctorId(cell.getValue().getDoctorId())));
        doctorColumn.setCellValueFactory(cell -> new SimpleStringProperty(getDoctorNameById(cell.getValue().getDoctorId())));

        dateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPreferredDate().format(dateFormatter))
        );        timeColumn.setCellValueFactory(cell -> new SimpleStringProperty("")); // If time is not stored, leave empty
        notesColumn.setCellValueFactory(cell -> cell.getValue().notesProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());

        loadAppointments();
    }

    private void preloadDoctorDetails() {
        doctorIdLookup.clear();
        doctorDepartmentLookup.clear();

        try {
            ResultSet rs = DatabaseHandler.getInstance().execQuery("SELECT doctor_id, name, department FROM doctor");
            while (rs.next()) {
                int id = rs.getInt("doctor_id");
                String name = rs.getString("name");
                String dept = rs.getString("department");

                doctorIdLookup.put(id, name);
                doctorDepartmentLookup.put(id, dept);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getDoctorNameById(int doctorId) {
        // You can use a Map<Integer, String> that was preloaded from DB to avoid repeated queries
        return doctorIdLookup.getOrDefault(doctorId, "Unknown");
    }

    private String getDepartmentNameFromDoctorId(int doctorId) {
        // Lookup or mock return
        return doctorDepartmentLookup.getOrDefault(doctorId, "General");
    }

    @FXML
    private void loadAppointments() {
        appointments.clear();
        try {
            ObservableList<Appointment> loaded = DatabaseHandler.getInstance().loadAppointments();
            appointments.addAll(loaded);
            appointmentTable.setItems(appointments);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
        private void onSearch(KeyEvent event) {
            String filter = searchField.getText().toLowerCase();
            appointmentTable.setItems(appointments.filtered(a ->
                    a.getName().toLowerCase().contains(filter) ||
                            a.getEmail().toLowerCase().contains(filter) ||
                            a.getStatus().toLowerCase().contains(filter)
            ));
        }

    public void approveAppointment() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String message = messageField.getText().trim();
            boolean success = AppointmentDAO.approveAppointmentWithMessage(selected.getAppointmentId(), message);

            if (success) {
                DatabaseHandler.sendNotification(
                        selected.getEmail(),
                        "Appointment approved. " + (message.isEmpty() ? "" : "Note: " + message)
                );
                loadAppointments(); // Refresh view
            }
        }
    }


    @FXML
    private void handleCancel() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean success = DatabaseHandler.getInstance().cancelAppointment(
                    selected.getEmail(),
                    selected.getPreferredDate().toString()   // or time if you have it
            );
            if(success) loadAppointments();
        }
    }

}


