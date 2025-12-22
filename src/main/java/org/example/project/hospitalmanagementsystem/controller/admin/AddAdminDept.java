package org.example.project.hospitalmanagementsystem.controller.admin;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.TextFieldListCell;
import org.example.project.hospitalmanagementsystem.controller.users.DepartmentCard;
import org.example.project.hospitalmanagementsystem.controller.hospital.DepartmentService;
import org.example.project.hospitalmanagementsystem.model.Department;
import org.example.project.hospitalmanagementsystem.model.DepartmentModel;
import org.example.project.hospitalmanagementsystem.model.Doctor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddAdminDept {

    @FXML private TextField departmentNameField;
    @FXML private TextArea departmentDescField;
    @FXML private TextField operatingHoursField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ListView<String> doctorListView;
    @FXML private ListView<String> servicesListView;
    @FXML private TextField serviceInputField;

    @FXML private Button addDepartmentButton;  // This can be your Save button
    @FXML private Button backButton;            // Add this button for Back


    private final DepartmentService departmentService = new DepartmentService();
    private DepartmentCard departmentController;

    @FXML
    public void initialize() {
        servicesListView.setEditable(true);
        servicesListView.setCellFactory(TextFieldListCell.forListView());

        servicesListView.setOnEditCommit(event -> {
            int index = event.getIndex();
            String newValue = event.getNewValue().trim();
            if (!newValue.isEmpty() && !servicesListView.getItems().contains(newValue)) {
                servicesListView.getItems().set(index, newValue);
            } else {
                String previousValue = servicesListView.getItems().get(index);
                servicesListView.getItems().set(index, previousValue);
            }
        });

        statusComboBox.setItems(FXCollections.observableArrayList("Active", "Closed"));
        loadDoctors();
    }

    private DepartmentCard departmentUser;

    public void setDepartmentUser(DepartmentCard departmentUser) {
        this.departmentUser = departmentUser;
    }


    private void loadDoctors() {
        List<Doctor> doctors = departmentService.getAllDoctorObjects();
        ObservableList<String> doctorNames = FXCollections.observableArrayList();
        for (Doctor doc : doctors) {
            doctorNames.add(doc.getName());
        }
        doctorListView.setItems(doctorNames);
        doctorListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setDepartmentController(DepartmentCard controller, Department dept) {
        this.departmentController = controller;
        if (dept != null) {
            this.departmentController.addDepartmentCard((dept)); //line 81
        } else {
            System.out.println("No department yet, skipping DepartmentModel creation.");
        }
    }


    @FXML
    private void handleSaveDepartment(ActionEvent event) {
        String name = departmentNameField.getText();
        String desc = departmentDescField.getText();
        String hours = operatingHoursField.getText();
        String status = statusComboBox.getValue();
        List<String> selectedDoctorNames = new ArrayList<>(doctorListView.getSelectionModel().getSelectedItems());

        if (name.isEmpty() || desc.isEmpty() || hours.isEmpty() || status == null || selectedDoctorNames.isEmpty()) {
            showAlert("All fields must be filled correctly.");
            return;
        }

        List<Integer> doctorIds = departmentService.getDoctorIdsByNames(selectedDoctorNames);

        List<String> services = new ArrayList<>(servicesListView.getItems());
        if (services.isEmpty()) {
            showAlert("At least one service must be added.");
            return;
        }

        Department newDept = new Department(0, name, desc, hours, 0, 10, doctorIds, LocalDate.now(), true, services);
        newDept.setServices(services);

        boolean success = departmentService.saveDepartment(newDept);

        if (success) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ManageDepartments.fxml"));
                Parent root = loader.load();
                ManageDepartmentController manageController = loader.getController();
                manageController.initialize(null, null); // manually reloads departments

                Stage stage = (Stage) departmentNameField.getScene().getWindow();
                stage.setScene(new Scene(root,1400,800));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAddService() {
        String service = serviceInputField.getText().trim();
        if (!service.isEmpty() && !servicesListView.getItems().contains(service)) {
            servicesListView.getItems().add(service);
            serviceInputField.clear();
        }
    }

    @FXML
    private void handleRemoveService() {
        String selected = servicesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            servicesListView.getItems().remove(selected);
        }
    }

    public void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/admin/ManageDepartments.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root,1400,800));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


// Note: The DepartmentService class should contain getAllDoctors(), saveDepartment(), and notifyFrontendToAddCard() methods.
// The Department model should be updated to include doctor assignment.
// The frontend (DepartmentController) should listen for the callback to add cards without needing a reload.


