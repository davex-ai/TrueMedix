package org.example.project.hospitalmanagementsystem.controller.users;

 import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
 import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.controller.admin.ComplaintDAO;
 import java.io.File;
import java.io.IOException;

public class Complaint {

        @FXML private TextField nameField;
        @FXML private TextArea messageArea;
        @FXML private ComboBox<String> categoryCombo;
        @FXML private ComboBox<String> urgencyCombo;
        @FXML private TextField phoneField;
        @FXML private Label screenshotLabel;
               private  String userEmail;

        private File selectedScreenshot;

        private final ComplaintDAO complaintDAO = new ComplaintDAO();

        @FXML
        public void initialize() {
            urgencyCombo.getSelectionModel().clearSelection();
            categoryCombo.getSelectionModel().clearSelection();

        }

        @FXML
        private void handleUpload() {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Screenshot");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File file = fileChooser.showOpenDialog(new Stage());
            if (file != null) {
                selectedScreenshot = file;
                screenshotLabel.setText(file.getName());
            }
        }

    @FXML
    private void handleSubmit() {
        String name = nameField.getText();
        String message = messageArea.getText();
        String category = categoryCombo.getValue();
        String urgency = urgencyCombo.getValue();
        String phone = phoneField.getText();
        String screenshotPath = selectedScreenshot != null ? selectedScreenshot.getAbsolutePath() : null;


        if (name.isEmpty() || message.isEmpty() || category.isEmpty() || phone.isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields.");
            return;
        }

        boolean success = complaintDAO.insertComplaint(
                userEmail, name, category, urgency, phone, message, screenshotPath
        );

        if (success) {
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
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        }public void setUserEmail(String email) {
        this.userEmail = email;
    }
    public void handleBack(MouseEvent mouseEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/user/Homepage.fxml"));
            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.centerOnScreen();
            stage.setTitle("TruMedix - Home");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
