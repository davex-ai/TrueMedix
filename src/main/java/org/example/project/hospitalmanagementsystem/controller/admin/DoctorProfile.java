package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.project.hospitalmanagementsystem.model.Doctor;

import java.io.File;

public class DoctorProfile {

        @FXML
        private Label nameLabel;
        @FXML private Label specializationLabel;
        @FXML private Label departmentLabel;
        @FXML private Label statusLabel;
        @FXML private Label salaryLabel;
        @FXML private Label dateLabel;
        @FXML private TextArea bioArea;
        @FXML private TextArea historyArea;
        @FXML private ImageView profileImage;

        public void setDoctorData(Doctor doctor) {
            nameLabel.setText(doctor.getName());
            specializationLabel.setText(doctor.getSpecialization());
            departmentLabel.setText(doctor.getDepartment());
            statusLabel.setText(doctor.getStatus().toString());
            salaryLabel.setText(String.valueOf(doctor.getSalary()));
            dateLabel.setText(doctor.getEmploymentDate().toString());
            bioArea.setText(doctor.getBio());
            historyArea.setText(doctor.getHistory());
            profileImage.setImage(new Image(new File(doctor.getPhotoPath()).toURI().toString()));
        }
    }


