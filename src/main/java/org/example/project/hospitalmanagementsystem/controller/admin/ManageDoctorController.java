package org.example.project.hospitalmanagementsystem.controller.admin;// ManageDoctorController.java

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.FlowPane;


import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.model.Doctor;

import java.io.IOException;
import java.util.List;

public class ManageDoctorController {

    @FXML
    private FlowPane cardContainer;

    @FXML
    private StackedBarChart<String, Number> salaryChart;
    @FXML
    private CategoryAxis categoryAxis;
    @FXML
    private NumberAxis numberAxis;

    @FXML
    public void initialize() {


        loadDoctorCards();
    }

    private void loadDoctorCards() {
        cardContainer.getChildren().clear();
        List<Doctor> doctors = DoctorDAO.getAllDoctors();

        for (Doctor doctor : doctors) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DoctorCard.fxml"));
                Node card = loader.load();

                DoctorCardController controller = loader.getController();
                controller.setDoctorData(doctor);
                cardContainer.getChildren().add(card);
                updateChart(doctors);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleAddNewDoctor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/add_doctor.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Doctor");
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadDoctorCards());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void updateChart(List<Doctor> doctors) {
        salaryChart.getData().clear();

        XYChart.Series<String, Number> salarySeries = new XYChart.Series<>();
        salarySeries.setName("Salary");

        XYChart.Series<String, Number> appointmentsSeries = new XYChart.Series<>();
        appointmentsSeries.setName("Pending Appointments");

        for (Doctor doctor : doctors) {
            String name = doctor.getName();
            salarySeries.getData().add(new XYChart.Data<>(name, doctor.getSalary()));
            appointmentsSeries.getData().add(new XYChart.Data<>(name, doctor.getPendingAppointmentsCount()));
        }

        salaryChart.getData().addAll(salarySeries, appointmentsSeries);
    }
    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/admin/adminpage.fxml"));
            Stage stage = (Stage) cardContainer.getScene().getWindow(); // or any other visible node
            Scene newScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(newScene);
            stage.setMaximized(true);
        }catch (IOException e){
            e.printStackTrace();
        }


    }

}
