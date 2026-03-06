package org.example.project.hospitalmanagementsystem.controller.users;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.example.project.hospitalmanagementsystem.controller.admin.DoctorDAO;
import org.example.project.hospitalmanagementsystem.model.Department;
import org.example.project.hospitalmanagementsystem.model.Doctor;

import java.io.IOException;
import java.util.List;

public class DepartmentView {

    @FXML private Label nameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label hoursLabel;
    @FXML private Label slotsLabel;
    @FXML private Label statusLabel;
    @FXML private Label doctorCountLabel;
    @FXML private Label statDoctorsLabel;
    @FXML private Label statSlotsLabel;
    @FXML private Label statServicesLabel;
    @FXML private VBox  doctorContainer;
    @FXML private VBox  servicesContainer;

    private Department department;

    public void setDepartment(Department department) {
        this.department = department;
        loadDepartmentData();
    }

    private void loadDepartmentData() {
        nameLabel.setText(department.getName());
        descriptionLabel.setText(department.getDescription());
        hoursLabel.setText("🕐 " + department.getOperatingHours());
        slotsLabel.setText("🗓 " + department.getAvailableSlots() + " slots available");
        statusLabel.setText(department.isActive() ? "● Active" : "● Inactive");

        List<Doctor> doctors = DoctorDAO.getDoctorsByDepartment(department.getName());
        int serviceCount = department.getServices() == null ? 0 : department.getServices().size();

        statDoctorsLabel.setText(String.valueOf(doctors.size()));
        statSlotsLabel.setText(String.valueOf(department.getAvailableSlots()));
        statServicesLabel.setText(String.valueOf(serviceCount));
        doctorCountLabel.setText(doctors.size() + " specialist" + (doctors.size() == 1 ? "" : "s") + " in this department");

        buildServicesList();
        buildDoctorCards(doctors);
    }

    private void buildServicesList() {
        servicesContainer.getChildren().clear();
        List<String> services = department.getServices();
        if (services == null || services.isEmpty()) {
            Label empty = new Label("No services listed.");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ab0c4; -fx-font-family: 'Segoe UI';");
            servicesContainer.getChildren().add(empty);
            return;
        }
        for (String service : services) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color: #f4f7fb; -fx-background-radius: 8;");
            Label dot = new Label("✦");
            dot.setStyle("-fx-text-fill: #1a6fbd; -fx-font-size: 10px;");
            Label lbl = new Label(service);
            lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #3d5a73; -fx-font-family: 'Segoe UI';");
            row.getChildren().addAll(dot, lbl);
            servicesContainer.getChildren().add(row);
        }
    }

    private void buildDoctorCards(List<Doctor> doctors) {
        doctorContainer.getChildren().clear();
        if (doctors.isEmpty()) {
            Label empty = new Label("No doctors listed for this department.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ab0c4; -fx-font-family: 'Segoe UI'; -fx-padding: 16 0 16 0;");
            doctorContainer.getChildren().add(empty);
            return;
        }
        for (Doctor doctor : doctors) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/doctorcard.fxml"));
                Node card = loader.load();
                DocCard ctrl = loader.getController();
                ctrl.setDoctorData(doctor);
                doctorContainer.getChildren().add(card);
            } catch (IOException e) {
                doctorContainer.getChildren().add(buildFallbackDoctorRow(doctor));
            }
        }
    }

    private HBox buildFallbackDoctorRow(Doctor doctor) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle("-fx-background-color: #f4f7fb; -fx-background-radius: 10;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label("Dr. " + doctor.getName());
        name.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a2e4a; -fx-font-family: 'Segoe UI';");
        Label spec = new Label(doctor.getSpecialization());
        spec.setStyle("-fx-font-size: 11px; -fx-text-fill: #7a8fa6; -fx-font-family: 'Segoe UI';");
        info.getChildren().addAll(name, spec);

        String badgeStyle = "ACTIVE".equalsIgnoreCase(doctor.getStatus().name())
                ? "-fx-background-color: #d4f5e2; -fx-text-fill: #1a7a45;"
                : "-fx-background-color: #fde8e8; -fx-text-fill: #b03030;";
        Label badge = new Label(doctor.getStatus().name());
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; " + badgeStyle);

        row.getChildren().addAll(info, badge);
        return row;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavHelper.goTo("/fxml/user/departmentuserCard.fxml", "Departments", (Node) event.getSource());
    }

    @FXML
    private void handleBookAppointment(MouseEvent event) {
        NavHelper.goTo("/fxml/user/appointment.fxml", "Book Appointment", (Node) event.getSource());
    }
}