package org.example.project.hospitalmanagementsystem.controller.admin;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Admin {
    @FXML
    private AnchorPane contentPane;  // the placeholder pane in center where views load

        @FXML private Label adminNameLabel;
        @FXML private Label patientCountLabel;
        @FXML private Label appointmentCountLabel;
        @FXML private Label doctorCountLabel;
    private Label adminEmailLabel;
    @FXML private Label notificationBadge;
    @FXML private StackPane notificationWrapper;

     public void setAdminNameLabel(String name) {
        adminNameLabel.setText("Welcome admin " + name);
    }
        @FXML
        public void initialize() {
            adminNameLabel.setText("Welcome, Dr. Jane");
            patientCountLabel.getText();
            appointmentCountLabel.getText();
            doctorCountLabel.setText("6");

        }

    @FXML
    private void manageDoctors() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageDoctors.fxml"));
            Parent manageDoctorsView = loader.load();

            // Clear current content and add the new view
            contentPane.getChildren().clear();
            contentPane.getChildren().add(manageDoctorsView);

            // Optional: Make loaded view resize with the pane
            AnchorPane.setTopAnchor(manageDoctorsView, 0.0);
            AnchorPane.setBottomAnchor(manageDoctorsView, 0.0);
            AnchorPane.setLeftAnchor(manageDoctorsView, 0.0);
            AnchorPane.setRightAnchor(manageDoctorsView, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML private void managePatients(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManagePatients.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Manage Patients");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        @FXML private void viewAppointments() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/adminappointment.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("Manage Patients");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }        }

    @FXML
    private void viewDepartments(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageDepartments.fxml"));
            Parent departmentsView = loader.load();

            // Pass reference back to Admin if needed
            ManageDepartmentController controller = loader.getController();
            controller.setParentPane(contentPane); // for back button use

            contentPane.getChildren().setAll(departmentsView);

            AnchorPane.setTopAnchor(departmentsView, 0.0);
            AnchorPane.setBottomAnchor(departmentsView, 0.0);
            AnchorPane.setLeftAnchor(departmentsView, 0.0);
            AnchorPane.setRightAnchor(departmentsView, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @FXML private void viewComplaints() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/admincomplaintview.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Manage Complaints");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
     }


}


