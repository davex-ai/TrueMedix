package org.example.project.hospitalmanagementsystem.controller.users;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.controller.admin.ComplaintDAO;

import java.io.File;

public class Complaint {

    @FXML private TextField        nameField;
    @FXML private TextArea         messageArea;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> urgencyCombo;
    @FXML private TextField        phoneField;
    @FXML private Label            screenshotLabel;
    @FXML private Label            feedbackLabel;

    private File   selectedScreenshot;
    private String userEmail;

    private final ComplaintDAO complaintDAO = new ComplaintDAO();

    @FXML
    public void initialize() {
        urgencyCombo.getSelectionModel().clearSelection();
        categoryCombo.getSelectionModel().clearSelection();
        userEmail = UserSession.getInstance().getUserEmail();
    }

    @FXML
    private void handleUpload() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Screenshot");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fc.showOpenDialog(new Stage());
        if (file != null) {
            selectedScreenshot = file;
            screenshotLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleSubmit() {
        String name     = nameField.getText().trim();
        String message  = messageArea.getText().trim();
        String category = categoryCombo.getValue();
        String urgency  = urgencyCombo.getValue();
        String phone    = phoneField.getText().trim();
        String path     = selectedScreenshot != null ? selectedScreenshot.getAbsolutePath() : null;

        if (name.isEmpty() || message.isEmpty() || category == null || phone.isEmpty()) {
            showFeedback("Please fill in all required fields.", false);
            return;
        }

        if (complaintDAO.insertComplaint(userEmail, name, category, urgency, phone, message, path)) {
            showFeedback("✓  Complaint submitted! We'll be in touch within 24 hours.", true);
            clearForm();
        } else {
            showFeedback("Failed to submit. Please try again later.", false);
        }
    }

    private void showFeedback(String message, boolean success) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle(
                "-fx-font-size: 13px; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;"
                        + "-fx-padding: 10 14 10 14; -fx-background-radius: 8; -fx-border-radius: 8;"
                        + (success
                        ? "-fx-background-color: #d4f5e2; -fx-text-fill: #1a7a45;"
                        : "-fx-background-color: #fde8e8; -fx-text-fill: #b03030;")
        );
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }

    private void clearForm() {
        nameField.clear();
        messageArea.clear();
        categoryCombo.getSelectionModel().clearSelection();
        urgencyCombo.getSelectionModel().clearSelection();
        phoneField.clear();
        selectedScreenshot = null;
        screenshotLabel.setText("No file selected");
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    @FXML
    public void handleBack(MouseEvent event) {
        NavHelper.goHome((Node) event.getSource());
    }
}