package org.example.project.hospitalmanagementsystem.controller.users;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.project.hospitalmanagementsystem.model.Doctor;
public class DocCard {

        @FXML
        private Label nameLabel;

        @FXML
        private Label yoeLabel;

        @FXML
        private Label specializationLabel;

        // This method is called from DepartmentView to set doctor info
        public void setDoctorData(Doctor doctor) {
            nameLabel.setText(doctor.getName());
            yoeLabel.setText("Years of Experience: " + doctor.getExperienceYears());
            specializationLabel.setText("Specialization: " + doctor.getSpecialization());
        }
    }


