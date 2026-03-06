package org.example.project.hospitalmanagementsystem.controller.users;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.example.project.hospitalmanagementsystem.controller.admin.ComplaintDAO;
import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;
import org.example.project.hospitalmanagementsystem.model.Appointment;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;

public class homepage {

    @FXML private Label      usernameLabel;
    @FXML private Label      welcomeLabel;
    @FXML private Label      greetingLabel;
    @FXML private Label      dateLabel;
    @FXML private Label      tipLabel;
    @FXML private Label      reminder;
    @FXML private Label      nextAppointmentLabel;
    @FXML private Label      statTotalLabel;
    @FXML private Label      statUpcomingLabel;
    @FXML private Label      statCompletedLabel;
    @FXML private Label      statPendingLabel;
    @FXML private VBox       appointmentsContainer;
    @FXML private Label      noAppointmentsLabel;
    @FXML private MenuButton bellBtn;

    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final File         tipFile      = new File("last_tip.txt");

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

        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            applySession(session.getUserName(), session.getUserEmail());
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> Platform.runLater(this::loadTipOfTheDay), 1, 1, TimeUnit.HOURS);
    }

    public void setUsername(String fullname) {
        UserSession.getInstance().setUserName(fullname);
        applySession(fullname, UserSession.getInstance().getUserEmail());
    }

    public void setUserEmail(String email) {
        UserSession.getInstance().setUserEmail(email);
        applySession(UserSession.getInstance().getUserName(), email);
    }

    private void applySession(String name, String email) {
        if (name != null && !name.isEmpty()) {
            usernameLabel.setText(name);
            welcomeLabel.setText("Welcome back, " + name + " 👋");
            greetingLabel.setText(getTimeGreeting());
        }
        if (email != null && !email.isEmpty()) {
            loadUserAppointments(email);
            loadAdminNotifications(email);
        }
    }

    private String getTimeGreeting() {
        int h = LocalDateTime.now().getHour();
        if (h < 12) return "Good morning! Hope you're feeling well today.";
        if (h < 17) return "Good afternoon! Don't forget to stay hydrated.";
        return "Good evening! Remember to rest and recover.";
    }

    private void loadUserAppointments(String email) {
        List<Appointment> list = fetchAppointmentsForUser(email);

        statTotalLabel.setText(String.valueOf(list.size()));
        statUpcomingLabel.setText(String.valueOf(list.stream().filter(a -> "Approved".equalsIgnoreCase(a.getStatus()) || "Pending".equalsIgnoreCase(a.getStatus())).count()));
        statCompletedLabel.setText(String.valueOf(list.stream().filter(a -> "Completed".equalsIgnoreCase(a.getStatus())).count()));
        statPendingLabel.setText(String.valueOf(list.stream().filter(a -> "Pending".equalsIgnoreCase(a.getStatus())).count()));

        list.stream()
                .filter(a -> !a.getPreferredDate().isBefore(LocalDate.now()))
                .min(Comparator.comparing(Appointment::getPreferredDate))
                .ifPresentOrElse(
                        next -> nextAppointmentLabel.setText(
                                next.getPreferredDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                        + " — Dr. " + getDoctorName(next.getDoctorId()) + "\n" + next.getStatus()),
                        () -> nextAppointmentLabel.setText("No upcoming appointments.")
                );

        appointmentsContainer.getChildren().clear();
        if (list.isEmpty()) {
            noAppointmentsLabel.setVisible(true);
            noAppointmentsLabel.setManaged(true);
            appointmentsContainer.getChildren().add(noAppointmentsLabel);
        } else {
            noAppointmentsLabel.setVisible(false);
            noAppointmentsLabel.setManaged(false);
            list.forEach(a -> appointmentsContainer.getChildren().add(buildAppointmentRow(a)));
        }
    }

    private HBox buildAppointmentRow(Appointment appt) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle("-fx-background-color: #f4f7fb; -fx-background-radius: 10;");

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.setMinWidth(52);
        Label day = new Label(appt.getPreferredDate().format(DateTimeFormatter.ofPattern("dd")));
        day.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a6fbd; -fx-font-family: 'Segoe UI';");
        Label month = new Label(appt.getPreferredDate().format(DateTimeFormatter.ofPattern("MMM")));
        month.setStyle("-fx-font-size: 11px; -fx-text-fill: #7a8fa6; -fx-font-family: 'Segoe UI';");
        dateBox.getChildren().addAll(day, month);

        Separator sep = new Separator();
        sep.setOrientation(Orientation.VERTICAL);
        sep.setStyle("-fx-opacity: 0.3;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label docLabel = new Label("Dr. " + getDoctorName(appt.getDoctorId()));
        docLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a2e4a; -fx-font-family: 'Segoe UI';");
        String notes = appt.getNotes() != null ? appt.getNotes().get() : null;
        Label noteLabel = new Label(notes != null && !notes.isEmpty() ? notes : "No notes");        noteLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7a8fa6; -fx-font-family: 'Segoe UI';");
        info.getChildren().addAll(docLabel, noteLabel);

        String bg, fg;
        switch (appt.getStatus().toLowerCase()) {
            case "approved":  bg = "#d4f5e2"; fg = "#1a7a45"; break;
            case "pending":   bg = "#fef6e4"; fg = "#c47e0a"; break;
            case "completed": bg = "#e8eeff"; fg = "#3b48c4"; break;
            case "cancelled": bg = "#fde8e8"; fg = "#b03030"; break;
            default:          bg = "#e8f0f8"; fg = "#3d5a73"; break;
        }
        Label badge = new Label(appt.getStatus());
        badge.setPadding(new Insets(3, 10, 3, 10));
        badge.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";");

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
                        rs.getInt("appointment_id"), rs.getString("name"), rs.getString("email"),
                        rs.getInt("user_id"), rs.getInt("doctor_id"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getString("status"), rs.getString("notes")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private String getDoctorName(int doctorId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM doctor WHERE doctor_id = ?")) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("name");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Unknown";
    }

    private void loadAdminNotifications(String email) {
        bellBtn.getItems().clear();
        bellBtn.getItems().add(new MenuItem("Your appointment is confirmed."));
        bellBtn.getItems().add(new MenuItem("Lab results uploaded."));
        complaintDAO.getResponsesForUser(email).forEach(r -> bellBtn.getItems().add(new MenuItem(r)));
    }

    private void loadTipOfTheDay() {
        String tip;
        LocalDateTime now = LocalDateTime.now();
        try {
            if (tipFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(tipFile));
                String timeStr = reader.readLine();
                String lastTip = reader.readLine();
                reader.close();
                LocalDateTime lastTime;
                try { lastTime = LocalDateTime.parse(timeStr); }
                catch (DateTimeParseException e) { lastTime = LocalDate.parse(timeStr).atStartOfDay(); }
                tip = Duration.between(lastTime, now).toHours() < 24 ? lastTip : getRandomTip();
                if (Duration.between(lastTime, now).toHours() >= 24) saveTip(now, tip);
            } else {
                tip = getRandomTip();
                saveTip(now, tip);
            }
        } catch (Exception e) { tip = getRandomTip(); }
        tipLabel.setText(tip);
    }

    private void saveTip(LocalDateTime time, String tip) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(tipFile))) { w.write(time + "\n" + tip); }
    }

    private String getRandomTip() { return tips.get(new Random().nextInt(tips.size())); }

    @FXML private void handleBookAppointment(MouseEvent e)   { NavHelper.goTo("/fxml/user/appointment.fxml",       "Book Appointment", (Node) e.getSource()); }
    @FXML private void handleBookAppointmentBtn(ActionEvent e){ NavHelper.goTo("/fxml/user/appointment.fxml",       "Book Appointment", (Node) e.getSource()); }
    @FXML private void handleDepartmentPage(MouseEvent e)    { NavHelper.goTo("/fxml/user/departmentuserCard.fxml", "Departments",      (Node) e.getSource()); }
    @FXML private void handleDepartmentBtn(ActionEvent e)    { NavHelper.goTo("/fxml/user/departmentuserCard.fxml", "Departments",      (Node) e.getSource()); }
    @FXML private void handleComplaintPage(MouseEvent e)     { NavHelper.goTo("/fxml/user/complaint.fxml",          "Complaint",        (Node) e.getSource()); }
    @FXML private void handleComplaintBtn(ActionEvent e)     { NavHelper.goTo("/fxml/user/complaint.fxml",          "Complaint",        (Node) e.getSource()); }
}