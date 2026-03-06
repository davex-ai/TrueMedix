package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.model.Doctor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManageDoctorController {



    @FXML private FlowPane cardContainer;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label            resultsLabel;

    @FXML private Label statTotalLabel;
    @FXML private Label statActiveLabel;
    @FXML private Label statOnLeaveLabel;
    @FXML private Label statAvgSalaryLabel;
    @FXML private Label statPendingLabel;

    @FXML private BarChart<String, Number>  salaryChart;
    @FXML private CategoryAxis             salaryXAxis;
    @FXML private NumberAxis               salaryYAxis;

    @FXML private BarChart<String, Number>  appointmentsChart;
    @FXML private CategoryAxis             apptXAxis;
    @FXML private NumberAxis               apptYAxis;

    @FXML private BarChart<String, Number>  departmentChart;
    @FXML private CategoryAxis             deptXAxis;
    @FXML private NumberAxis               deptYAxis;



    private List<Doctor> allDoctors;

    @FXML
    public void initialize() {
        allDoctors = DoctorDAO.getAllDoctors();

        populateFilterCombos();
        setupSearchAndFilters();
        renderAll(allDoctors);
    }

    private void populateFilterCombos() {
        List<String> departments = allDoctors.stream()
                .map(Doctor::getDepartment)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        departments.add(0, "All Departments");
        departmentFilter.setItems(FXCollections.observableArrayList(departments));
        departmentFilter.setValue("All Departments");

        List<String> statuses = allDoctors.stream()
                .map(d -> capitalize(d.getStatus().name()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        statuses.add(0, "All Statuses");
        statusFilter.setItems(FXCollections.observableArrayList(statuses));
        statusFilter.setValue("All Statuses");
    }

    private void setupSearchAndFilters() {
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        departmentFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String dept   = departmentFilter.getValue();
        String status = statusFilter.getValue();

        List<Doctor> filtered = allDoctors.stream()
                .filter(d -> search.isEmpty()
                        || d.getName().toLowerCase().contains(search)
                        || d.getSpecialization().toLowerCase().contains(search))
                .filter(d -> dept == null || dept.equals("All Departments")
                        || d.getDepartment().equalsIgnoreCase(dept))
                .filter(d -> status == null || status.equals("All Statuses")
                        || capitalize(d.getStatus().name()).equalsIgnoreCase(status))
                .collect(Collectors.toList());

        renderCards(filtered);
        updateStats(filtered);
        updateCharts(filtered);
        resultsLabel.setText(filtered.size() + " doctor" + (filtered.size() == 1 ? "" : "s") + " found");
    }

    private void renderAll(List<Doctor> doctors) {
        renderCards(doctors);
        updateStats(doctors);
        updateCharts(doctors);
        resultsLabel.setText(doctors.size() + " doctor" + (doctors.size() == 1 ? "" : "s") + " found");
    }

    private void renderCards(List<Doctor> doctors) {
        cardContainer.getChildren().clear();
        for (Doctor doctor : doctors) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/admin/DoctorCard.fxml"));
                Node card = loader.load();
                DoctorCardController ctrl = loader.getController();
                ctrl.setDoctorData(doctor);
                cardContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateStats(List<Doctor> doctors) {
        long total    = doctors.size();
        long active   = doctors.stream().filter(d -> d.getStatus() == Doctor.DoctorStatus.ACTIVE).count();
        long onLeave  = doctors.stream().filter(d -> d.getStatus() == Doctor.DoctorStatus.ON_LEAVE).count();
        double avgSal = doctors.stream().mapToDouble(Doctor::getSalary).average().orElse(0);
        long pending  = doctors.stream().mapToLong(d -> AppointmentDAO.getPendingCountByDoctorId(d.getId())).sum();
        statPendingLabel.setText(String.valueOf(pending));

        statTotalLabel.setText(String.valueOf(total));
        statActiveLabel.setText(String.valueOf(active));
        statOnLeaveLabel.setText(String.valueOf(onLeave));
        statAvgSalaryLabel.setText(String.format("$%,.0f", avgSal));
        statPendingLabel.setText(String.valueOf(pending));
    }

    private void updateCharts(List<Doctor> doctors) {
        updateSalaryChart(doctors);
        updateAppointmentsChart(doctors);
        updateDepartmentChart(doctors);
    }

    private void updateSalaryChart(List<Doctor> doctors) {
        salaryChart.getData().clear();
        salaryChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Salary");


        doctors.stream()
                .sorted((a, b) -> Double.compare(b.getSalary(), a.getSalary()))
                .limit(10)
                .forEach(d -> series.getData().add(
                        new XYChart.Data<>(shortName(d.getName()), d.getSalary())));

        salaryChart.getData().add(series);
        styleBarChart(salaryChart, "#1e77d4");
    }

    private void updateAppointmentsChart(List<Doctor> doctors) {
        appointmentsChart.getData().clear();
        appointmentsChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pending Appointments");

        doctors.stream()
                .sorted((a, b) -> Long.compare(
                        AppointmentDAO.getPendingCountByDoctorId(b.getId()),
                        AppointmentDAO.getPendingCountByDoctorId(a.getId())))
                .limit(10)
                .forEach(d -> {
                    long pendingCount = AppointmentDAO.getPendingCountByDoctorId(d.getId());
                    series.getData().add(new XYChart.Data<>(shortName(d.getName()), pendingCount));
                });

        appointmentsChart.getData().add(series);
        styleBarChart(appointmentsChart, "#b03030");
    }

    private void updateDepartmentChart(List<Doctor> doctors) {
        departmentChart.getData().clear();
        departmentChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Count");

        Map<String, Long> byDept = doctors.stream()
                .collect(Collectors.groupingBy(Doctor::getDepartment, Collectors.counting()));

        byDept.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));

        departmentChart.getData().add(series);
        styleBarChart(departmentChart, "#1a7a45");
    }

    /** Tint all bars in a single-series BarChart after data is added. */
    private void styleBarChart(BarChart<String, Number> chart, String hex) {
        chart.lookupAll(".bar").forEach(node ->
                node.setStyle("-fx-bar-fill: " + hex + "; -fx-background-radius: 4 4 0 0;"));
        if (!chart.getData().isEmpty()) {
            chart.getData().get(0).getData().forEach(d -> {
                if (d.getNode() != null)
                    d.getNode().setStyle("-fx-bar-fill: " + hex + "; -fx-background-radius: 4 4 0 0;");
            });
        }
    }

    @FXML
    public void handleAddNewDoctor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin/add_doctor.fxml"));
            Parent root = loader.load();
            Stage modal = new Stage();
            modal.setTitle("Add New Doctor");
            modal.setScene(new Scene(root));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(cardContainer.getScene().getWindow());
            modal.setOnHiding(e -> reload());
            modal.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        allDoctors = DoctorDAO.getAllDoctors();
        populateFilterCombos();
        applyFilters();
    }

    private String shortName(String fullName) {
        if (fullName == null) return "";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return fullName;

        return parts[0].charAt(0) + ". " + parts[parts.length - 1];
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase().replace("_", " ");
    }
}