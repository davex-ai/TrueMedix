package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;

import java.io.IOException;
import java.sql.*;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Admin {

    // ── Sidebar nav buttons ────────────────────────────────────────────────
    @FXML private VBox   sidebar;
    @FXML private Button btnDashboard;
    @FXML private Button btnPatients;
    @FXML private Button btnDoctors;
    @FXML private Button btnAppointments;
    @FXML private Button btnComplaints;
    @FXML private Label pendingDoctorsLabel;
    @FXML private Button btnDepartments;

    @FXML private Label     adminNameLabel;
    @FXML private Label     sidebarAdminName;

    // ── Summary cards ──────────────────────────────────────────────────────
    @FXML private Label patientCountLabel;
    @FXML private Label appointmentCountLabel;
    @FXML private Label doctorCountLabel;
    @FXML private Label deptCountLabel;

    // ── Charts ─────────────────────────────────────────────────────────────
    @FXML private BarChart<String, Number>  appointmentsBarChart;
    @FXML private PieChart                  departmentPieChart;

    // ─────────────────────────────────────────────────────────────────────
    // Model for the recent-appointments table
    // ─────────────────────────────────────────────────────────────────────


    // ─────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setActiveButton(btnDashboard);
        loadDashboardData();
    }

    /** Called by the login controller to personalise the welcome labels. */
    public void setAdminName(String name) {
        adminNameLabel.setText(name);
        sidebarAdminName.setText(name);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Data loading – ALL read from DB, NO hardcoded values
    // ─────────────────────────────────────────────────────────────────────
    private void loadDashboardData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            loadSummaryCards(conn);
            loadAppointmentsBarChart(conn);
            loadTopPendingDoctor(conn);
            loadDepartmentPieChart(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSummaryCards(Connection conn) throws SQLException {
        // --- Total patients ---
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) FROM users WHERE role = 'patient'")) {
            if (rs.next()) patientCountLabel.setText(String.valueOf(rs.getInt(1)));
        }

        // --- Today's appointments ---
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) " +
                             "FROM appointments " +
                             "WHERE STR_TO_DATE(appointment_date, '%Y-%m-%d') = CURDATE()")) {

            if (rs.next()) {
                appointmentCountLabel.setText(String.valueOf(rs.getInt(1)));
            }
        }

        // --- Active doctors ---
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) FROM doctor WHERE status = 'ACTIVE'")) {
            if (rs.next()) doctorCountLabel.setText(String.valueOf(rs.getInt(1)));
        }

        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) FROM department_stats")) {
            if (rs.next()) deptCountLabel.setText(String.valueOf(rs.getInt(1)));
        }
    }

    private void loadAppointmentsBarChart(Connection conn) throws SQLException {
        // Monthly appointment counts for the current year
        String sql =
                "SELECT MONTH(appointment_date) AS m, COUNT(*) AS cnt " +
                        "FROM appointments " +
                        "WHERE YEAR(appointment_date) = YEAR(CURDATE()) " +
                        "GROUP BY m ORDER BY m";

        Map<String, Number> data = new LinkedHashMap<>();
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                String monthName = Month.of(rs.getInt("m"))
                        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                data.put(monthName, rs.getInt("cnt"));
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Appointments");
        data.forEach((month, cnt) -> series.getData().add(
                new XYChart.Data<>(month, cnt)));

        appointmentsBarChart.getData().clear();
        appointmentsBarChart.getData().add(series);
    }

    private void loadTopPendingDoctor(Connection conn) throws SQLException {
        String sql =
                "SELECT d.name, COUNT(*) AS cnt " +
                        "FROM appointments a " +
                        "JOIN doctor d ON a.doctor_id = d.doctor_id " +
                        "WHERE a.status = 'Pending' " +
                        "GROUP BY d.doctor_id, d.name " +
                        "ORDER BY cnt DESC " +
                        "LIMIT 5";

        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            StringBuilder topDoctors = new StringBuilder();
            int count = 0;

            while (rs.next() && count < 5) {
                topDoctors.append(rs.getString("name"))
                        .append(" (").append(rs.getInt("cnt")).append(")\n");
                count++;
            }

            if (topDoctors.length() > 0) {
                // Remove the last newline
                topDoctors.setLength(topDoctors.length() - 1);
                pendingDoctorsLabel.setText(topDoctors.toString());
            } else {
                pendingDoctorsLabel.setText("None 🎉");
            }
        }
    }

    private void loadDepartmentPieChart(Connection conn) throws SQLException {

        String sql =
                "SELECT ds.department, COUNT(d.doctor_id) AS cnt " +
                        "FROM department_stats ds " +
                        "LEFT JOIN doctor d ON d.doctor_id = ds.id " +
                        "GROUP BY ds.department " +
                        "ORDER BY cnt DESC";

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {
                pieData.add(new PieChart.Data(
                        rs.getString("department"),
                        rs.getInt("cnt")
                ));
            }
        }

        departmentPieChart.setData(pieData);
    }



    // ─────────────────────────────────────────────────────────────────────
    // Sidebar navigation
    // ─────────────────────────────────────────────────────────────────────
    @FXML private void showDashboard()    { setActiveButton(btnDashboard);    /* already on dashboard */ }

    @FXML private void manageDoctors() {
        setActiveButton(btnDoctors);
        openInNewWindow("/fxml/admin/ManageDoctors.fxml", "Manage Doctors");
    }

    @FXML private void managePatients(ActionEvent event) {
        setActiveButton(btnPatients);
        openInNewWindow("/fxml/admin/ManagePatients.fxml", "Manage Patients");
    }

    @FXML private void viewAppointments() {
        setActiveButton(btnAppointments);
        openInNewWindow("/fxml/admin/adminappointment.fxml", "Appointments");
    }

    @FXML private void viewDepartments(ActionEvent event) {
        setActiveButton(btnDepartments);
        openInNewWindow("/fxml/admin/ManageDepartments.fxml", "Departments");
    }

    @FXML private void viewComplaints() {
        setActiveButton(btnComplaints);
        openInNewWindow("/fxml/admin/admincomplaintview.fxml", "Complaints");
    }

    @FXML private void openNotifications() {
        // TODO: open notification panel / popover
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────

    /** Highlight the chosen button and remove highlight from the others. */
    private void setActiveButton(Button active) {
        for (Button b : new Button[]{btnDashboard, btnPatients, btnDoctors,
                btnAppointments, btnComplaints, btnDepartments}) {
            b.getStyleClass().remove("sidebar-button-active");
        }
        if (!active.getStyleClass().contains("sidebar-button-active")) {
            active.getStyleClass().add("sidebar-button-active");
        }
    }

    /** Open a sub-page in a new window. */
    private void openInNewWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper called by the Departments sub-page if it needs a back-button
     * reference to the parent pane (kept for backward compatibility with
     * ManageDepartmentController#setParentPane).
     */
    public AnchorPane getContentPane() { return null; /* Not used in BorderPane layout */ }
}