package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.model.Doctor;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import javafx.scene.input.MouseEvent;
import java.io.IOException;

public class DoctorCardController {
    @FXML private Label nameLabel, specializationLabel, departmentLabel, experienceLabel;
    @FXML private Label employmentDateLabel, salaryLabel, pendingAppointmentsLabel;
    @FXML private Label statusBadge;
    @FXML private Rectangle heatmapBar;
    @FXML private ImageView doctorImage;
    @FXML private Button optionsButton;
    @FXML private VBox expandedSection;
    @FXML private Label bioLabel, historyLabel;
    private Doctor doctor;



    private boolean expanded = false;

    public void setDoctorData(Doctor doctor) {
        nameLabel.setText(doctor.getName());
        specializationLabel.setText(doctor.getSpecialization());
        departmentLabel.setText(doctor.getDepartment());
        employmentDateLabel.setText("Employed: " + doctor.getEmploymentDate());
        salaryLabel.setText("Salary: $" + doctor.getSalary());
        pendingAppointmentsLabel.setText("Pending Appointments: " + doctor.getPendingAppointmentsCount());
        experienceLabel.setText(doctor.getExperienceYears() + " years experience");

        try{
            Image image = new Image("file:" + doctor.getPhotoPath(), true);
            doctorImage.setImage(image);
        }catch (Exception e){
            Image placeholder = new Image(getClass().getResourceAsStream("/media/avatar.png"));
            doctorImage.setImage(placeholder);
        }


        // Status Badge
        switch (doctor.getStatus()) {
            case ACTIVE -> statusBadge.setStyle("-fx-background-color: #38a169; -fx-text-fill: white;");
            case ON_LEAVE -> statusBadge.setStyle("-fx-background-color: #ecc94b; -fx-text-fill: black;");
            case RETIRED -> statusBadge.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white;");
        }
        statusBadge.setText(doctor.getStatus().toString().replace("_", " "));

        // Heatmap
        int intensity = Math.min(doctor.getPendingAppointmentsCount() * 10, 255);
        heatmapBar.setStyle("-fx-fill: rgb(" + intensity + ",0,0);");

        // Quick Action Menu
        ContextMenu menu = new ContextMenu();
        MenuItem delete = new MenuItem("Delete");
        menu.getItems().addAll( delete);
        optionsButton.setOnMouseClicked(e -> menu.show(optionsButton, Side.BOTTOM, 0, 0));

        delete.setOnAction(e -> {

                DoctorDAO.deleteDoctorById(doctor.getDoctorId());

            Node card = optionsButton.getParent().getParent(); // Assuming the card layout is two levels up
            ((Pane) card.getParent()).getChildren().remove(card);
        });

        // Expanded content
        bioLabel.setText("Bio: " + doctor.getBio());
        historyLabel.setText("History: " + doctor.getHistory());
        this.doctor = doctor;
    }

    @FXML
    private void toggleExpanded(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DoctorProfile.fxml"));
            Parent root = loader.load();

            DoctorProfile controller = loader.getController();
            controller.setDoctorData(doctor); // Pass selected doctor object

            Stage stage = new Stage();
            stage.setTitle("Doctor Profile");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
