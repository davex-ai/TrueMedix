package org.example.project.hospitalmanagementsystem.controller.users;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;
import org.example.project.hospitalmanagementsystem.controller.admin.ComplaintDAO;

import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class homepage {

    @FXML
    private Complaint complaintController;
    private String userEmail;
    @FXML
    private Label usernameLabel;
    private final ComplaintDAO complaintDAO = new ComplaintDAO(); // at top

    @FXML
    private Label greetingLabel;

    @FXML
    private Label reminder;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label tipLabel;

    @FXML
    private ListView<String> appointmentsList;

    @FXML
    private MenuButton bellBtn;

    private final File tipFile = new File("last_tip.txt");

    private final List<String> tips = List.of(
            "Drink 8 glasses of water a day!",
            "Exercise for at least 30 mins!",
            "Mental health matters — take breaks.",
            "Don’t skip breakfast!",
            "Get 7-8 hours of sleep."
    );


    @FXML
    public void initialize() {

        welcomeLabel.setText("Welcome to Trumedix");
        dateLabel.setText("Today is: " + LocalDate.now().toString());
        greetingLabel.setText("How are you today?");
        reminder.setText("Your payment plan will be finished next month");
        appointmentsList.getItems().addAll(
                "10:00 AM - Dr. Smith (Cardiology)",
                "2:30 PM - Dr. Lee (Dermatology)"
        );

//        bellBtn.getItems().clear();
//        bellBtn.getItems().addAll(
//                new MenuItem("Your appointment is confirmed."),
//                new MenuItem("New message from Dr. Clinton."),
//                new MenuItem("Lab results uploaded."),
//                new MenuItem("Your blood test is ready."),
//                new MenuItem("Message from Dr. Patel.")
//        );

        loadTipOfTheDay(); // Show tip right away

        // ➕ Start hourly tip updater
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::loadTipOfTheDay);
        }, 0, 1, TimeUnit.HOURS);
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
                    // Try parse full datetime first
                    lastTime = LocalDateTime.parse(timeStr);
                } catch (DateTimeParseException e) {
                    // Fallback: parse as LocalDate and convert to LocalDateTime at start of day
                    LocalDate lastDate = LocalDate.parse(timeStr);
                    lastTime = lastDate.atStartOfDay();
                }

                if (Duration.between(lastTime, now).toHours() < 24) {
                    tip = lastTip;  // reuse same tip if under 24 hours
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

        tipLabel.setText("Tip of the Day: " + tip);
    }


    private void saveTip(LocalDateTime time, String tip) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tipFile))) {
            writer.write(time.toString() + "\n" + tip);
        }
    }
    private String getRandomTip() {
        return tips.get(new Random().nextInt(tips.size()));
    }

        @FXML
        private void handleComplaintForm(MouseEvent event) {
            System.out.println("Redirect to complaint form...");
            // TODO: Load complaint form FXML
              try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/complaint.fxml"));
                    Parent root = loader.load();
                    Complaint controller = loader.getController();
                     controller.setUserEmail(userEmail);
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setTitle("Complaint");
                    stage.setScene(new Scene(root, 1400,800));
                    stage.setMaximized(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        @FXML
        private void handleBookAppointment(MouseEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/appointment.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root, 1400,800));
                stage.setTitle("Appointments");
                stage.setMaximized(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    public void setUsername(String fullname) {
        usernameLabel.setText(fullname);
    }

    @FXML
    private void handleDepartmentPage(MouseEvent event)throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/departmentuserCard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("Departments");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        if (complaintController != null) {
            complaintController.setUserEmail(userEmail);
        }
        loadAdminNotifications();
    }

    public Complaint getComplaintController() {
        return complaintController;
    }
    private void loadAdminNotifications() {
        bellBtn.getItems().clear();

        // Add predefined static notifications (optional)
        bellBtn.getItems().addAll(
                new MenuItem("Your appointment is confirmed."),
                new MenuItem("Lab results uploaded.")
        );

        // Now fetch dynamic responses
        if (userEmail != null && !userEmail.isEmpty()) {
            List<String> responses = complaintDAO.getResponsesForUser(userEmail);
            for (String response : responses) {
                MenuItem item = new MenuItem(response);
                bellBtn.getItems().add(item);
            }
        }
    }
}


