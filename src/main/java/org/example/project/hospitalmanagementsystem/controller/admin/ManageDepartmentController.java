package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.controller.hospital.DepartmentService;
import org.example.project.hospitalmanagementsystem.model.Department;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ManageDepartmentController implements Initializable {

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private TilePane         departmentTilePane;
    @FXML private Label            resultsLabel;

    @FXML private Label totalDepartmentsLabel, mostVisitedLabel, mostDoctorsLabel;
    @FXML private Label statActiveLabel, statInactiveLabel, statTotalVisitsLabel, statSlotsLabel;

    @FXML private AnchorPane parentPane;

    @FXML private BarChart<String, Number> visitsChart, slotsChart, doctorsChart;
    @FXML private CategoryAxis visitsChartX, slotsChartX, doctorsChartX;
    @FXML private NumberAxis   visitsChartY, slotsChartY, doctorsChartY;

    private List<Department>     departments;
    private Map<String, Integer> appointmentsByDept;
    private Map<String, Integer> doctorCountByDept;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filterCombo.getItems().addAll("All", "Active", "Inactive");
        filterCombo.setValue("All");
        loadFromService();
    }

    public void loadFromService() {
        DepartmentService svc = new DepartmentService();
        departments        = svc.getAllDepartments();
        appointmentsByDept = svc.getAppointmentCountByDepartment();
        doctorCountByDept  = svc.getDoctorCountByDepartment();
        updateStats();
        applyFilters();
        updateCharts(departments);
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        String filter  = filterCombo.getValue();

        List<Department> filtered = departments.stream()
                .filter(d -> filter.equals("All") || d.isActive() == filter.equals("Active"))
                .filter(d -> d.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        renderCards(filtered);
        updateCharts(filtered);
        resultsLabel.setText(filtered.size() + " department" + (filtered.size() == 1 ? "" : "s"));
    }

    private void renderCards(List<Department> list) {
        departmentTilePane.getChildren().clear();
        for (Department dept : list)
            departmentTilePane.getChildren().add(createDepartmentCard(dept));
    }

    private VBox createDepartmentCard(Department dept) {
        int realAppointments = appointmentsByDept.getOrDefault(dept.getName(), 0);
        int realDoctors      = doctorCountByDept.getOrDefault(dept.getName(), 0);

        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setPrefSize(270, 215);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 3);"
        );

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (dept.isActive() ? "#1a7a45" : "#b03030") + ";");

        Label name = new Label(dept.getName());
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e4a; -fx-font-family: 'Segoe UI';");
        HBox.setHgrow(name, Priority.ALWAYS);

        Label statusBadge = new Label(dept.isActive() ? "Active" : "Inactive");
        statusBadge.setPadding(new Insets(2, 8, 2, 8));
        statusBadge.setStyle(
                "-fx-background-radius: 10; -fx-border-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;" +
                        (dept.isActive()
                                ? "-fx-background-color: #d4f5e2; -fx-text-fill: #1a7a45;"
                                : "-fx-background-color: #fde8e8; -fx-text-fill: #b03030;")
        );
        titleRow.getChildren().addAll(statusDot, name, statusBadge);

        Label desc = new Label(dept.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #5a7a94; -fx-font-family: 'Segoe UI';");
        desc.setMaxHeight(42);

        HBox metaRow = new HBox(14);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label appts = new Label("📋 " + realAppointments + " appointments");
        appts.setStyle("-fx-font-size: 11px; -fx-text-fill: #3d5a73; -fx-font-family: 'Segoe UI';");

        Label slots = new Label("🗓 " + dept.getAvailableSlots() + " slots");
        slots.setStyle("-fx-font-size: 11px; -fx-text-fill: #3d5a73; -fx-font-family: 'Segoe UI';");

        Label doctors = new Label("👨‍⚕️ " + realDoctors + " doctors");
        doctors.setStyle("-fx-font-size: 11px; -fx-text-fill: #3d5a73; -fx-font-family: 'Segoe UI';");

        metaRow.getChildren().addAll(appts, slots, doctors);

        Label hours = new Label("🕐 " + dept.getOperatingHours());
        hours.setStyle("-fx-font-size: 11px; -fx-text-fill: #7a8fa6; -fx-font-family: 'Segoe UI';");

        String servicesText = dept.getServices() == null || dept.getServices().isEmpty()
                ? "No services listed"
                : String.join(" · ", dept.getServices());
        Label services = new Label(servicesText);
        services.setWrapText(true);
        services.setStyle("-fx-font-size: 11px; -fx-text-fill: #7a8fa6; -fx-font-family: 'Segoe UI';");

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.3;");

        card.getChildren().addAll(titleRow, desc, sep, metaRow, hours, services);
        return card;
    }

    private void updateStats() {
        if (departments == null || departments.isEmpty()) return;

        long active    = departments.stream().filter(Department::isActive).count();
        long inactive  = departments.size() - active;
        int  slots     = departments.stream().mapToInt(Department::getAvailableSlots).sum();
        int  totalAppts = appointmentsByDept.values().stream().mapToInt(Integer::intValue).sum();

        String topVisited = appointmentsByDept.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        totalDepartmentsLabel.setText(String.valueOf(departments.size()));
        statActiveLabel.setText(String.valueOf(active));
        statInactiveLabel.setText(String.valueOf(inactive));
        statTotalVisitsLabel.setText(String.valueOf(totalAppts));
        statSlotsLabel.setText(String.valueOf(slots));
        mostVisitedLabel.setText(topVisited);

        if (mostDoctorsLabel != null) {
            String topDoctors = doctorCountByDept.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("—");
            mostDoctorsLabel.setText(topDoctors);
        }
    }

    private void updateCharts(List<Department> list) {
        buildAppointmentsChart(list);
        buildSlotsChart(list);
        buildDoctorsChart(list);
    }

    private void buildAppointmentsChart(List<Department> list) {
        visitsChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        list.stream()
                .sorted((a, b) -> Integer.compare(
                        appointmentsByDept.getOrDefault(b.getName(), 0),
                        appointmentsByDept.getOrDefault(a.getName(), 0)))
                .limit(10)
                .forEach(d -> s.getData().add(new XYChart.Data<>(
                        shortName(d.getName()),
                        appointmentsByDept.getOrDefault(d.getName(), 0))));
        visitsChart.getData().add(s);
        tintBars(s, "#1e77d4");
    }

    private void buildSlotsChart(List<Department> list) {
        slotsChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        list.stream()
                .sorted((a, b) -> Integer.compare(b.getAvailableSlots(), a.getAvailableSlots()))
                .limit(10)
                .forEach(d -> s.getData().add(new XYChart.Data<>(
                        shortName(d.getName()), d.getAvailableSlots())));
        slotsChart.getData().add(s);
        tintBars(s, "#1a7a45");
    }

    private void buildDoctorsChart(List<Department> list) {
        doctorsChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        list.stream()
                .sorted((a, b) -> Integer.compare(
                        doctorCountByDept.getOrDefault(b.getName(), 0),
                        doctorCountByDept.getOrDefault(a.getName(), 0)))
                .limit(10)
                .forEach(d -> s.getData().add(new XYChart.Data<>(
                        shortName(d.getName()),
                        doctorCountByDept.getOrDefault(d.getName(), 0))));
        doctorsChart.getData().add(s);
        tintBars(s, "#c47e0a");
    }

    private void tintBars(XYChart.Series<String, Number> s, String hex) {
        s.getData().forEach(d -> {
            if (d.getNode() != null)
                d.getNode().setStyle("-fx-bar-fill: " + hex + "; -fx-background-radius: 4 4 0 0;");
        });
    }

    private String shortName(String name) {
        if (name == null) return "";
        return name.length() > 14 ? name.substring(0, 12) + "…" : name;
    }

    @FXML private void onSearch()        { applyFilters(); }
    @FXML private void onFilterChanged() { applyFilters(); }

    @FXML
    private void onResetFilters() {
        searchField.clear();
        filterCombo.setValue("All");
        applyFilters();
    }

    @FXML
    private void onAddDepartment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminDept.fxml"));
            Parent root = loader.load();

            // Create a BRAND NEW stage
            Stage stage = new Stage();
            stage.setTitle("Add Department");
            stage.setScene(new Scene(root, 1400, 800));

            AddAdminDept ctrl = loader.getController();
            ctrl.setDepartmentController(null, null);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setParentPane(AnchorPane parentPane) {
        this.parentPane = parentPane;
    }

}