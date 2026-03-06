package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;

import java.io.IOException;
import java.sql.*;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Admin {

    @FXML private VBox   sidebar;
    @FXML private Button btnDashboard;
    @FXML private Button btnPatients;
    @FXML private Button btnDoctors;
    @FXML private Button btnAppointments;
    @FXML private Button btnComplaints;
    @FXML private Button btnDepartments;

    @FXML private Label     pageTitleLabel;
    @FXML private Label     adminNameLabel;
    @FXML private Label     sidebarAdminName;


    @FXML private StackPane contentArea;
    @FXML private ScrollPane dashboardView;

    @FXML private Label patientCountLabel;
    @FXML private Label patientTrendLabel;
    @FXML private Label appointmentCountLabel;
    @FXML private Label appointmentTrendLabel;
    @FXML private Label doctorCountLabel;
    @FXML private Label doctorTrendLabel;
    @FXML private Label deptCountLabel;
    @FXML private Label deptTrendLabel;
    @FXML private Label pendingDoctorsLabel;


    @FXML private BarChart<String, Number> appointmentsBarChart;
    @FXML private PieChart                 departmentPieChart;

    @FXML private TableView<RecentAppointment>            recentAppointmentsTable;
    @FXML private TableColumn<RecentAppointment, Integer> colApptId;
    @FXML private TableColumn<RecentAppointment, String>  colApptPatient;
    @FXML private TableColumn<RecentAppointment, String>  colApptDoctor;
    @FXML private TableColumn<RecentAppointment, String>  colApptDepartment;
    @FXML private TableColumn<RecentAppointment, String>  colApptDate;
    @FXML private TableColumn<RecentAppointment, String>  colApptStatus;

    public static class RecentAppointment {
        private final int    id;
        private final String patientName;
        private final String doctorName;
        private final String department;
        private final String date;
        private final String status;

        public RecentAppointment(int id, String patientName, String doctorName,
                                 String department, String date, String status) {
            this.id = id; this.patientName = patientName;
            this.doctorName = doctorName; this.department = department;
            this.date = date; this.status = status;
        }
        public int    getId()          { return id; }
        public String getPatientName() { return patientName; }
        public String getDoctorName()  { return doctorName; }
        public String getDepartment()  { return department; }
        public String getDate()        { return date; }
        public String getStatus()      { return status; }
    }

    @FXML
    public void initialize() {
        setActiveButton(btnDashboard);
        loadDashboardData();
    }

    public void setAdminName(String name) {
        adminNameLabel.setText(name);
        sidebarAdminName.setText(name);
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard() {
        setActiveButton(btnDashboard);
        pageTitleLabel.setText("Admin Dashboard");

        contentArea.getChildren().setAll(dashboardView);
    }

    @FXML
    private void manageDoctors() {
        setActiveButton(btnDoctors);
        pageTitleLabel.setText("Doctors");
        loadPage("/fxml/admin/ManageDoctors.fxml");
    }

    @FXML
    private void managePatients() {
        setActiveButton(btnPatients);
        pageTitleLabel.setText("Patients");
        loadPage("/fxml/admin/ManagePatients.fxml");
    }

    @FXML
    private void viewAppointments() {
        setActiveButton(btnAppointments);
        pageTitleLabel.setText("Appointments");
        loadPage("/fxml/admin/adminappointment.fxml");
    }

    @FXML
    private void viewDepartments() {
        setActiveButton(btnDepartments);
        pageTitleLabel.setText("Departments");
        loadPage("/fxml/admin/ManageDepartments.fxml");
    }

    @FXML
    private void viewComplaints() {
        setActiveButton(btnComplaints);
        pageTitleLabel.setText("Complaints");
        loadPage("/fxml/admin/admincomplaintview.fxml");
    }

    private void setActiveButton(Button active) {
        for (Button b : new Button[]{btnDashboard, btnPatients, btnDoctors,
                btnAppointments, btnComplaints, btnDepartments}) {
            b.getStyleClass().remove("sidebar-button-active");
        }
        if (!active.getStyleClass().contains("sidebar-button-active")) {
            active.getStyleClass().add("sidebar-button-active");
        }
    }

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

        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'patient'")) {
            if (rs.next()) patientCountLabel.setText(String.valueOf(rs.getInt(1)));
        }


        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) FROM appointments " +
                             "WHERE STR_TO_DATE(appointment_date, '%Y-%m-%d') = CURDATE()")) {
            if (rs.next()) appointmentCountLabel.setText(String.valueOf(rs.getInt(1)));
        }


        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM doctor WHERE status = 'ACTIVE'")) {
            if (rs.next()) doctorCountLabel.setText(String.valueOf(rs.getInt(1)));
        }


        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM department_stats")) {
            if (rs.next()) deptCountLabel.setText(String.valueOf(rs.getInt(1)));
        }
    }

    private void loadAppointmentsBarChart(Connection conn) throws SQLException {
        String sql =
                "SELECT MONTH(appointment_date) AS m, COUNT(*) AS cnt " +
                        "FROM appointments " +
                        "WHERE YEAR(appointment_date) = YEAR(CURDATE()) " +
                        "GROUP BY m ORDER BY m";

        Map<String, Number> data = new LinkedHashMap<>();
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                String month = Month.of(rs.getInt("m"))
                        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                data.put(month, rs.getInt("cnt"));
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Appointments");
        data.forEach((m, cnt) -> series.getData().add(new XYChart.Data<>(m, cnt)));

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
                        "ORDER BY cnt DESC LIMIT 5";

        StringBuilder sb = new StringBuilder();
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                sb.append(rs.getString("name"))
                        .append("  (").append(rs.getInt("cnt")).append(")\n");
            }
        }
        pendingDoctorsLabel.setText(sb.length() > 0
                ? sb.toString().stripTrailing()
                : "None 🎉");
    }

    private void loadDepartmentPieChart(Connection conn) throws SQLException {
        String sql =
                "SELECT ds.department, COUNT(d.doctor_id) AS cnt " +
                        "FROM department_stats ds " +
                        "LEFT JOIN doctor d ON d.doctor_id = ds.id " +
                        "GROUP BY ds.department ORDER BY cnt DESC";

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                pieData.add(new PieChart.Data(rs.getString("department"), rs.getInt("cnt")));
            }
        }
        departmentPieChart.setData(pieData);
    }

}