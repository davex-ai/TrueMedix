package org.example.project.hospitalmanagementsystem.controller.admin;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import org.example.project.hospitalmanagementsystem.model.Complaint;

public class AdminComplaintsController {
    @FXML private TableView<Complaint> complaintTable;
    @FXML private TextField searchField;

    @FXML private TableColumn<Complaint, String> nameColumn, emailColumn, messageColumn, statusColumn, submittedAtColumn;
    @FXML private TableColumn<Complaint, String> categoryColumn, urgencyColumn, phoneColumn, screenshotPathColumn;

    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private ObservableList<Complaint> complaints = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
//        emailColumn.setCellValueFactory(cell -> cell.getValue().emailProperty());
        messageColumn.setCellValueFactory(cell -> cell.getValue().messageProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        submittedAtColumn.setCellValueFactory(cell -> cell.getValue().submittedAtProperty());

        categoryColumn.setCellValueFactory(cell -> cell.getValue().categoryProperty());
        urgencyColumn.setCellValueFactory(cell -> cell.getValue().urgencyProperty());
        phoneColumn.setCellValueFactory(cell -> cell.getValue().phoneProperty());
        screenshotPathColumn.setCellValueFactory(cell -> cell.getValue().screenshotPathProperty());

        loadComplaints();
    }


    @FXML
    private void loadComplaints() {
        complaints.setAll(complaintDAO.getAllComplaints());
        complaintTable.setItems(complaints);
        complaintTable.setMinSize(1000,600);
    }

    @FXML
    private void handleSearch(KeyEvent event) {
        String filter = searchField.getText().toLowerCase();
        complaintTable.setItems(complaints.filtered(c ->
                        c.getMessage().toLowerCase().contains(filter) ||
                        c.getCategory().toLowerCase().contains(filter)
        ));
    }

    @FXML
    private void handleDelete() {
        Complaint selected = complaintTable.getSelectionModel().getSelectedItem();
        if (selected != null && complaintDAO.deleteComplaint(selected.getId())) {
            loadComplaints();
        }
    }

    @FXML
    private void handleRespond() {
        Complaint selected = complaintTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Respond to Complaint");
            dialog.setContentText("Enter your response:");

            dialog.showAndWait().ifPresent(response -> {
                if (complaintDAO.respondToComplaint(selected.getId(), response)) {
                    loadComplaints();
                    showAlert("Response Sent", "The complaint was successfully responded to.");
                } else {
                    showAlert("Error", "Failed to send response.");
                }
            });
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
