package org.example.project.hospitalmanagementsystem.controller.admin;// AddDoctorController.java

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.model.Doctor;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class AddDoctorController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> departmentCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker employmentDatePicker;
    @FXML private TextField salaryField;
    @FXML private TextArea bioArea;
    @FXML private TextArea historyArea;
    @FXML private TextField specializationField;
    @FXML private ImageView photopreview;
    @FXML private int doctor_Id;

    private String photoPath = null;

    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("Active", "On Leave", "Retired"));
        departmentCombo.setItems(FXCollections.observableArrayList("Cardiology","Clinical(Check-ups)","Dermatology","Maternity","Neurology","Pediatrics"));

            }

    @FXML
    private void handlePhotoUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Doctor Photo");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            photoPath = file.getAbsolutePath(); // Store path or copy to /media/doctors/
            Image image = new Image(file.toURI().toString());
            photopreview.setImage(image);
        }
    }
    private void showError(String message, Color red) {
        showError(message, Color.RED);
    }

    @FXML
    private void handleAddDoctor(ActionEvent event) {
        try {
            String specialization = specializationField.getText();
            String department = departmentCombo.getValue();
            String name = nameField.getText();
            String statust = statusCombo.getValue();
            LocalDate employmentDate = employmentDatePicker.getValue();
            double salary = Double.parseDouble(salaryField.getText());
            String bio = bioArea.getText();
            String history = historyArea.getText();
            int pendingAppointmentCount = 0;


            if (specialization == null || department == null || name == null || statust == null || employmentDate == null ||  photoPath == null) {
                System.out.println("One or more fields are empty.");
                return;
            }

            Doctor.DoctorStatus status = Doctor.DoctorStatus.valueOf(statust.toUpperCase().replace(" ", "_"));

            Doctor newDoctor = new Doctor(doctor_Id ,specialization, department, name, employmentDate, salary, pendingAppointmentCount, photoPath, status,bio, history);

            DoctorDAO.insertDoctor(newDoctor);

            Integer doctorId = DoctorDAO.getdepartmentByName(department);
            if (doctorId == null) {
                showError("Doctor doesnt exist.", Color.RED);
                return;
            }
        }catch (NumberFormatException e){
            System.out.println("Invalid salary entered");
        }catch (Exception e){
            e.printStackTrace();
        }
        ((Stage) nameField.getScene().getWindow()).close();

        // Optionally clear form or go back
    }
    @FXML
    private void handleBack(ActionEvent event) {
        // Load the previous scene or main admin screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/adminpage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
