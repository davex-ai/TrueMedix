package org.example.project.hospitalmanagementsystem.controller.users;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.controller.admin.AppointmentDAO;
import org.example.project.hospitalmanagementsystem.controller.admin.ComplaintDAO;
import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;
import org.example.project.hospitalmanagementsystem.model.Appointment;

import java.io.*;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class homepage {

    @FXML private Label usernameLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label greetingLabel;
    @FXML private Label dateLabel;
    @FXML private Label tipLabel;
    @FXML private Label reminder;
    @FXML private Label nextAppointmentLabel;
    @FXML private Label statTotalLabel;
    @FXML private Label statUpcomingLabel;
    @FXML private Label statCompletedLabel;
    @FXML private Label statPendingLabel;
    @FXML private VBox  appointmentsContainer;
    @FXML private Label noAppointmentsLabel;
    @FXML private MenuButton bellBtn;

    private String userEmail;
    private String userName;

    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    private final File tipFile = new File("last_tip.txt");

    private final List<String> tips = List.of(
            "Drink 8 glasses of water a day!",
            "Exercise for at least 30 mins daily.",
            "Mental health matters — take breaks.",
            "Don't skip breakfast!",
            "Get 7-8 hours of sleep each night.",
            "Wash your hands frequently.",
            "Schedule regular health check-ups."
    );

    @FXML
    public void initialize() {
        dateLabel.setText("Today is " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy")));
        reminder.setText("Stay on top of your health — book regular check-ups.");
        loadTipOfTheDay();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> Platform.runLater(this::loadTipOfTheDay), 1, 1, TimeUnit.HOURS);
    }

    public void setUsername(String fullname) {
        this.userName = fullname;
        usernameLabel.setText(fullname);
        welcomeLabel.setText("Welcome back, " + fullname + " 👋");
        greetingLabel.setText(getTimeGreeting());
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
        loadUserAppointments();
        loadAdminNotifications();
    }

    private String getTimeGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Good morning! Hope you're feeling well today.";
        if (hour < 17) return "Good afternoon! Don't forget to stay hydrated.";
        return "Good evening! Remember to rest and recover.";
    }

    private void loadUserAppointments() {
        if (userEmail == null || userEmail.isEmpty()) return;

        List<Appointment> userAppointments = fetchAppointmentsForUser(userEmail);

        long total     = userAppointments.size();
        long upcoming  = userAppointments.stream().filter(a -> "Approved".equalsIgnoreCase(a.getStatus()) || "Pending".equalsIgnoreCase(a.getStatus())).count();
        long completed = userAppointments.stream().filter(a -> "Completed".equalsIgnoreCase(a.getStatus())).count();
        long pending   = userAppointments.stream().filter(a -> "Pending".equalsIgnoreCase(a.getStatus())).count();

        statTotalLabel.setText(String.valueOf(total));
        statUpcomingLabel.setText(String.valueOf(upcoming));
        statCompletedLabel.setText(String.valueOf(completed));
        statPendingLabel.setText(String.valueOf(pending));

        Appointment next = userAppointments.stream()
                .filter(a -> !a.getPreferredDate().isBefore(LocalDate.now()))
                .min((a, b) -> a.getPreferredDate().compareTo(b.getPreferredDate()))
                .orElse(null);

        if (next != null) {
            nextAppointmentLabel.setText(next.getPreferredDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    + " — Dr. " + getDoctorName(next.getDoctorId())
                    + "\n" + next.getStatus());
        } else {
            nextAppointmentLabel.setText("No upcoming appointments.");
        }

        appointmentsContainer.getChildren().clear();

        if (userAppointments.isEmpty()) {
            noAppointmentsLabel.setVisible(true);
            noAppointmentsLabel.setManaged(true);
            appointmentsContainer.getChildren().add(noAppointmentsLabel);
        } else {
            noAppointmentsLabel.setVisible(false);
            noAppointmentsLabel.setManaged(false);
            for (Appointment appt : userAppointments) {
                appointmentsContainer.getChildren().add(buildAppointmentRow(appt));
            }
        }
    }

    private HBox buildAppointmentRow(Appointment appt) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle(
                "-fx-background-color: #f4f7fb; -fx-background-radius: 10; -fx-border-radius: 10;"
        );

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.setMinWidth(52);
        Label day = new Label(appt.getPreferredDate().format(DateTimeFormatter.ofPattern("dd")));
        day.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a6fbd; -fx-font-family: 'Segoe UI';");
        Label month = new Label(appt.getPreferredDate().format(DateTimeFormatter.ofPattern("MMM")));
        month.setStyle("-fx-font-size: 11px; -fx-text-fill: #7a8fa6; -fx-font-family: 'Segoe UI';");
        dateBox.getChildren().addAll(day, month);

        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-opacity: 0.3;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        String doctorName = getDoctorName(appt.getDoctorId());
        Label docLabel = new Label("Dr. " + doctorName);
        docLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a2e4a; -fx-font-family: 'Segoe UI';");
        String notes = appt.getNotes() != null ? appt.getNotes().get() : null;
        Label noteLabel = new Label(notes != null && !notes.isEmpty() ? notes : "No notes");        noteLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7a8fa6; -fx-font-family: 'Segoe UI';");
        info.getChildren().addAll(docLabel, noteLabel);

        String status = appt.getStatus();
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

        row.getChildren().addAll(dateBox, sep, info, badge);
        return row;
    }

    private List<Appointment> fetchAppointmentsForUser(String email) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT appointment_id, name, email, user_id, doctor_id, appointment_date, status, notes " +
                "FROM appointments WHERE email = ? ORDER BY appointment_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("user_id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getString("status"),
                        rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private String getDoctorName(int doctorId) {
        String sql = "SELECT name FROM doctor WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void loadAdminNotifications() {
        bellBtn.getItems().clear();
        bellBtn.getItems().add(new MenuItem("Your appointment is confirmed."));
        bellBtn.getItems().add(new MenuItem("Lab results uploaded."));
        if (userEmail != null && !userEmail.isEmpty()) {
            List<String> responses = complaintDAO.getResponsesForUser(userEmail);
            for (String r : responses) {
                bellBtn.getItems().add(new MenuItem(r));
            }
        }
    }

    private void loadTipOfTheDay() {
        String tip = tips.get(0);
        LocalDateTime now = LocalDateTime.now();
        try {
            if (tipFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(tipFile));
                String timeStr = reader.readLine();
                String lastTip = reader.readLine();
                reader.close();
                LocalDateTime lastTime;
                try {
                    lastTime = LocalDateTime.parse(timeStr);
                } catch (DateTimeParseException e) {
                    lastTime = LocalDate.parse(timeStr).atStartOfDay();
                }
                if (Duration.between(lastTime, now).toHours() < 24) {
                    tip = lastTip;
                } else {
                    tip = getRandomTip();
                    saveTip(now, tip);
                }
            } else {
                tip = getRandomTip();
                saveTip(now, tip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tipLabel.setText(tip);
    }

    private void saveTip(LocalDateTime time, String tip) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tipFile))) {
            writer.write(time + "\n" + tip);
        }
    }

    private String getRandomTip() {
        return tips.get(new Random().nextInt(tips.size()));
    }

    @FXML
    private void handleBookAppointment(MouseEvent event) {
        navigateTo("/fxml/user/appointment.fxml", "Book Appointment", (Node) event.getSource());
    }

    @FXML
    private void handleBookAppointmentBtn(ActionEvent event) {
        navigateTo("/fxml/user/appointment.fxml", "Book Appointment", (Node) event.getSource());
    }

    @FXML
    private void handleDepartmentPage(MouseEvent event) {
        navigateTo("/fxml/user/departmentuserCard.fxml", "Departments", (Node) event.getSource());
    }

    @FXML
    private void handleDepartmentBtn(ActionEvent event) {
        navigateTo("/fxml/user/departmentuserCard.fxml", "Departments", (Node) event.getSource());
    }

    @FXML
    private void handleComplaintPage(MouseEvent event) {
        navigateTo("/fxml/user/complaint.fxml", "Complaint", (Node) event.getSource());
    }

    @FXML
    private void handleComplaintBtn(ActionEvent event) {
        navigateTo("/fxml/user/complaint.fxml", "Complaint", (Node) event.getSource());
    }

    private void navigateTo(String fxmlPath, String title, Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle(title);
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Complaint getComplaintController() {
        return null;
    }
}