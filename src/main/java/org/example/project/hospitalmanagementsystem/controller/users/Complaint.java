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
        String name     = nameField.getText();
        String message  = messageArea.getText();
        String category = categoryCombo.getValue();
        String urgency  = urgencyCombo.getValue();
        String phone    = phoneField.getText();
        String path     = selectedScreenshot != null ? selectedScreenshot.getAbsolutePath() : null;

        if (name.isEmpty() || message.isEmpty() || category == null || phone.isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields.");
            return;
        }

        if (complaintDAO.insertComplaint(userEmail, name, category, urgency, phone, message, path)) {
            showAlert("Success", "Complaint submitted successfully!");
            clearForm();
        } else {
            showAlert("Error", "Failed to submit complaint. Try again later.");
        }
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

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setContentText(content);
        a.showAndWait();
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    @FXML
    public void handleBack(MouseEvent event) {
        NavHelper.goHome((Node) event.getSource());
    }
}