package org.example.project.hospitalmanagementsystem.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import org.example.project.hospitalmanagementsystem.database.DatabaseHandler;
import org.example.project.hospitalmanagementsystem.model.Appointment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminAppointmentsController {

    // ─── FXML Bindings ─────────────────────────────────────────────────────────

    @FXML private TableView<Appointment>               appointmentTable;
    @FXML private TableColumn<Appointment, Integer>    idColumn;
    @FXML private TableColumn<Appointment, String>     nameColumn, emailColumn, departmentColumn,
            doctorColumn, timeColumn, notesColumn;
    @FXML private TableColumn<Appointment, String>     dateColumn, statusColumn;
    @FXML private TableColumn<Appointment, Void>       actionsColumn;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterStatus, filterDoctor, filterDepartment;
    @FXML private Button           approveButton, cancelButton;
    @FXML private TextArea         messageField;
    @FXML private Label            resultsLabel, paginationInfoLabel;

    // Stat labels
    @FXML private Label statTotalLabel, statPendingLabel, statApprovedLabel,
            statTodayLabel, statCancelledLabel, statCompletedLabel;

    // Charts
    @FXML private BarChart<String, Number> statusChart, doctorChart, departmentChart;
    @FXML private CategoryAxis statusChartX, doctorChartX, deptChartX;
    @FXML private NumberAxis   statusChartY, doctorChartY, deptChartY;

    // ─── Data ──────────────────────────────────────────────────────────────────

    private final ObservableList<Appointment> appointments     = FXCollections.observableArrayList();
    private       FilteredList<Appointment>   filteredList;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final Map<Integer, String> doctorIdLookup         = new HashMap<>();
    private final Map<Integer, String> doctorDepartmentLookup = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════════════════
    //  Init
    // ═══════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        preloadDoctorDetails();
        setupColumns();
        setupActionsColumn();
        setupFilters();
        setupDoubleClick();
        loadAppointments();
    }

    // ─── Doctor lookup (unchanged logic) ───────────────────────────────────────

    private void preloadDoctorDetails() {
        doctorIdLookup.clear();
        doctorDepartmentLookup.clear();
        try {
            ResultSet rs = DatabaseHandler.getInstance().execQuery(
                    "SELECT doctor_id, name, department FROM doctor");
            while (rs.next()) {
                int id = rs.getInt("doctor_id");
                doctorIdLookup.put(id, rs.getString("name"));
                doctorDepartmentLookup.put(id, rs.getString("department"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getDoctorNameById(int doctorId) {
        return doctorIdLookup.getOrDefault(doctorId, "Unknown");
    }

    private String getDepartmentNameFromDoctorId(int doctorId) {
        return doctorDepartmentLookup.getOrDefault(doctorId, "General");
    }

    // ─── Column Setup ──────────────────────────────────────────────────────────

    private void setupColumns() {
        idColumn.setCellValueFactory(cell -> cell.getValue().appointmentIdProperty().asObject());
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        emailColumn.setCellValueFactory(cell -> cell.getValue().emailProperty());

        departmentColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(getDepartmentNameFromDoctorId(cell.getValue().getDoctorId())));
        doctorColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(getDoctorNameById(cell.getValue().getDoctorId())));

        dateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPreferredDate().format(dateFormatter)));
        timeColumn.setCellValueFactory(cell -> new SimpleStringProperty(""));
        notesColumn.setCellValueFactory(cell -> cell.getValue().notesProperty());

        // Status column with colored badge
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }

                String bg, fg;
                switch (status.toLowerCase()) {
                    case "approved":  bg = "#d4f5e2"; fg = "#1a7a45"; break;
                    case "pending":   bg = "#fef6e4"; fg = "#c47e0a"; break;
                    case "completed": bg = "#e8eeff"; fg = "#3b48c4"; break;
                    case "cancelled": bg = "#fde8e8"; fg = "#b03030"; break;
                    default:          bg = "#e8f0f8"; fg = "#3d5a73"; break;
                }

                Label badge = new Label(status);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle(
                        "-fx-background-radius: 12; -fx-border-radius: 12;" +
                                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                                "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
                );
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        // Enable table sorting
        appointmentTable.getSortOrder().add(dateColumn);
    }

    // ─── Actions Column ────────────────────────────────────────────────────────

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button btnView    = makeBtn("View",    "#e8f1fa", "#1e77d4");
            private final Button btnApprove = makeBtn("Approve", "#d4f5e2", "#1a7a45");
            private final Button btnCancel  = makeBtn("Cancel",  "#fde8e8", "#b03030");

            private Button makeBtn(String label, String bg, String fg) {
                Button b = new Button(label);
                b.setStyle(
                        "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;" +
                                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 9 4 9;"
                );
                return b;
            }

            {
                btnView.setOnAction(e -> showDetailDialog(getTableView().getItems().get(getIndex())));
                btnApprove.setOnAction(e -> approveSingle(getTableView().getItems().get(getIndex())));
                btnCancel.setOnAction(e -> cancelSingle(getTableView().getItems().get(getIndex())));
            }

            private final HBox box = new HBox(5, btnView, btnApprove, btnCancel);
            { box.setAlignment(Pos.CENTER_LEFT); }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ─── Filter / Search Setup ────────────────────────────────────────────────

    private void setupFilters() {
        filterStatus.valueProperty().addListener((obs, o, n) -> applyAllFilters());
        filterDoctor.valueProperty().addListener((obs, o, n) -> applyAllFilters());
        filterDepartment.valueProperty().addListener((obs, o, n) -> applyAllFilters());
    }

    private void populateFilterCombos() {
        // Status
        List<String> statuses = appointments.stream()
                .map(Appointment::getStatus).distinct().sorted().collect(Collectors.toList());
        statuses.add(0, "All Statuses");
        filterStatus.setItems(FXCollections.observableArrayList(statuses));
        filterStatus.setValue("All Statuses");

        // Doctor
        List<String> doctors = appointments.stream()
                .map(a -> getDoctorNameById(a.getDoctorId())).distinct().sorted().collect(Collectors.toList());
        doctors.add(0, "All Doctors");
        filterDoctor.setItems(FXCollections.observableArrayList(doctors));
        filterDoctor.setValue("All Doctors");

        // Department
        List<String> depts = appointments.stream()
                .map(a -> getDepartmentNameFromDoctorId(a.getDoctorId())).distinct().sorted().collect(Collectors.toList());
        depts.add(0, "All Departments");
        filterDepartment.setItems(FXCollections.observableArrayList(depts));
        filterDepartment.setValue("All Departments");
    }

    private void applyAllFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String status = filterStatus.getValue();
        String doctor = filterDoctor.getValue();
        String dept   = filterDepartment.getValue();

        ObservableList<Appointment> result = appointments.filtered(a -> {
            boolean matchSearch = search.isEmpty()
                    || a.getName().toLowerCase().contains(search)
                    || a.getEmail().toLowerCase().contains(search)
                    || getDoctorNameById(a.getDoctorId()).toLowerCase().contains(search);

            boolean matchStatus = status == null || status.equals("All Statuses")
                    || a.getStatus().equalsIgnoreCase(status);

            boolean matchDoctor = doctor == null || doctor.equals("All Doctors")
                    || getDoctorNameById(a.getDoctorId()).equalsIgnoreCase(doctor);

            boolean matchDept = dept == null || dept.equals("All Departments")
                    || getDepartmentNameFromDoctorId(a.getDoctorId()).equalsIgnoreCase(dept);

            return matchSearch && matchStatus && matchDoctor && matchDept;
        });

        appointmentTable.setItems(result);
        resultsLabel.setText(result.size() + " record" + (result.size() == 1 ? "" : "s"));
        paginationInfoLabel.setText("Showing " + result.size() + " of " + appointments.size());
        updateAnalytics(result);
    }

    // ─── Double-click to view details ──────────────────────────────────────────

    private void setupDoubleClick() {
        appointmentTable.setRowFactory(tv -> {
            TableRow<Appointment> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !row.isEmpty())
                    showDetailDialog(row.getItem());
            });
            return row;
        });
    }

    // ─── Load Appointments (unchanged logic) ───────────────────────────────────

    @FXML
    private void loadAppointments() {
        appointments.clear();
        try {
            ObservableList<Appointment> loaded = DatabaseHandler.getInstance().loadAppointments();
            appointments.addAll(loaded);
        } catch (Exception e) {
            e.printStackTrace();
        }
        populateFilterCombos();
        applyAllFilters();
        updateStats(appointments);
        updateAnalytics(appointments);
    }

    // ─── onSearch (unchanged wiring, now delegates to shared filter) ───────────

    @FXML
    private void onSearch(KeyEvent event) {
        applyAllFilters();
    }

    // ─── Reset Filters ─────────────────────────────────────────────────────────

    @FXML
    private void resetFilters() {
        searchField.clear();
        filterStatus.setValue("All Statuses");
        filterDoctor.setValue("All Doctors");
        filterDepartment.setValue("All Departments");
        applyAllFilters();
    }

    // ─── Approve (unchanged logic, also wired from Actions column) ────────────

    @FXML
    public void approveAppointment() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) approveSingle(selected);
    }

    private void approveSingle(Appointment a) {
        String message = messageField.getText() == null ? "" : messageField.getText().trim();
        boolean success = AppointmentDAO.approveAppointmentWithMessage(a.getAppointmentId(), message);
        if (success) {
            DatabaseHandler.sendNotification(a.getEmail(),
                    "Appointment approved." + (message.isEmpty() ? "" : " Note: " + message));
            loadAppointments();
        }
    }

    // ─── Cancel (unchanged logic, also wired from Actions column) ─────────────

    @FXML
    private void handleCancel() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) cancelSingle(selected);
    }

    private void cancelSingle(Appointment a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel appointment for \"" + a.getName() + "\" on " +
                        a.getPreferredDate().format(dateFormatter) + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Cancellation");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                boolean ok = DatabaseHandler.getInstance().cancelAppointment(
                        a.getEmail(), a.getPreferredDate().toString());
                if (ok) loadAppointments();
            }
        });
    }

    // ─── Detail Dialog ─────────────────────────────────────────────────────────

    private void showDetailDialog(Appointment a) {
        String info =
                "Appointment ID : " + a.getAppointmentId()                       + "\n" +
                        "Patient        : " + a.getName()                                 + "\n" +
                        "Email          : " + a.getEmail()                                + "\n" +
                        "Doctor         : " + getDoctorNameById(a.getDoctorId())          + "\n" +
                        "Department     : " + getDepartmentNameFromDoctorId(a.getDoctorId()) + "\n" +
                        "Date           : " + a.getPreferredDate().format(dateFormatter)  + "\n" +
                        "Status         : " + a.getStatus()                               + "\n" +
                        "Notes          : " + (a.getNotes() == null ? "—" : a.getNotes());

        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("Appointment Details");
        dlg.setHeaderText(a.getName() + " — " + a.getPreferredDate().format(dateFormatter));
        dlg.setContentText(info);
        dlg.showAndWait();
    }

    // ─── Stub handlers for header buttons ─────────────────────────────────────

    @FXML
    private void handleNewAppointment() {
        showAlert(Alert.AlertType.INFORMATION, "New Appointment", "Open New Appointment form here.");
    }

    @FXML
    private void handleExport() {
        showAlert(Alert.AlertType.INFORMATION, "Export", "Export functionality — connect to CSV writer.");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Stats & Analytics
    // ═══════════════════════════════════════════════════════════════════════════

    private void updateStats(ObservableList<Appointment> list) {
        long total     = list.size();
        long pending   = count(list, "pending");
        long approved  = count(list, "approved");
        long cancelled = count(list, "cancelled");
        long completed = count(list, "completed");
        long today     = list.stream()
                .filter(a -> a.getPreferredDate().equals(LocalDate.now())).count();

        statTotalLabel.setText(String.valueOf(total));
        statPendingLabel.setText(String.valueOf(pending));
        statApprovedLabel.setText(String.valueOf(approved));
        statCancelledLabel.setText(String.valueOf(cancelled));
        statCompletedLabel.setText(String.valueOf(completed));
        statTodayLabel.setText(String.valueOf(today));
    }

    private void updateAnalytics(ObservableList<Appointment> list) {
        updateStatusChart(list);
        updateDoctorChart(list);
        updateDepartmentChart(list);
    }

    private void updateStatusChart(ObservableList<Appointment> list) {
        statusChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        Map<String, Long> grouped = list.stream()
                .collect(Collectors.groupingBy(a -> capitalize(a.getStatus()), Collectors.counting()));
        grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        statusChart.getData().add(s);
        tintBars(statusChart, "#1e77d4");
    }

    private void updateDoctorChart(ObservableList<Appointment> list) {
        doctorChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        Map<String, Long> grouped = list.stream()
                .collect(Collectors.groupingBy(
                        a -> shortName(getDoctorNameById(a.getDoctorId())), Collectors.counting()));
        grouped.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(8)
                .forEach(e -> s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        doctorChart.getData().add(s);
        tintBars(doctorChart, "#1a7a45");
    }

    private void updateDepartmentChart(ObservableList<Appointment> list) {
        departmentChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        Map<String, Long> grouped = list.stream()
                .collect(Collectors.groupingBy(
                        a -> getDepartmentNameFromDoctorId(a.getDoctorId()), Collectors.counting()));
        grouped.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .forEach(e -> s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        departmentChart.getData().add(s);
        tintBars(departmentChart, "#c47e0a");
    }

    private void tintBars(BarChart<String, Number> chart, String hex) {
        if (chart.getData().isEmpty()) return;
        chart.getData().get(0).getData().forEach(d -> {
            if (d.getNode() != null)
                d.getNode().setStyle("-fx-bar-fill: " + hex + "; -fx-background-radius: 4 4 0 0;");
        });
    }

    // ─── Utilities ─────────────────────────────────────────────────────────────

    private long count(ObservableList<Appointment> list, String status) {
        return list.stream().filter(a -> a.getStatus().equalsIgnoreCase(status)).count();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private String shortName(String full) {
        if (full == null) return "";
        String[] p = full.trim().split("\\s+");
        return p.length == 1 ? full : p[0].charAt(0) + ". " + p[p.length - 1];
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}