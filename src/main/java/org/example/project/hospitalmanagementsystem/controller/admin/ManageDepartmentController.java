package org.example.project.hospitalmanagementsystem.controller.admin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.controller.hospital.DepartmentService;
import org.example.project.hospitalmanagementsystem.model.Department;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ManageDepartmentController implements Initializable{
    // ManageDepartmentsController.java

        @FXML private TextField searchField;
        @FXML private ComboBox<String> filterCombo;
        @FXML private TilePane departmentTilePane;
        @FXML private Label totalDepartmentsLabel;
        @FXML private Label mostVisitedLabel;
        @FXML private Label mostDoctorsLabel;
        @FXML private AnchorPane parentPane;


    private List<Department> departments;

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            filterCombo.getItems().addAll("All", "Active", "Inactive");
            filterCombo.setValue("All");
            loadFromService();
            DepartmentService departmentService = new DepartmentService();
            departments = departmentService.getAllDepartments();
            updateAnalytics();
            loadDepartments("", "All");
        }

        private void loadDepartments(String keyword, String filter) {
            departmentTilePane.getChildren().clear();
            for (Department dept : departments) {
                if ((filter.equals("All") || dept.isActive() == filter.equals("Active")) &&
                        dept.getName().toLowerCase().contains(keyword.toLowerCase())) {
                    departmentTilePane.getChildren().add(createDepartmentCard(dept));
                }
            }
        }

        private VBox createDepartmentCard(Department dept) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setPrefSize(280, 180);
            card.getStyleClass().add("department-card");

            Label name = new Label(dept.getName());
            name.setFont(new Font("Arial", 20));

            Label desc = new Label(dept.getDescription());
            desc.setWrapText(true);

            Label created = new Label("Created: " + dept.getCreatedDate());

            card.getChildren().addAll(name, desc, created);
            return card;
        }

        private void updateAnalytics() {
            totalDepartmentsLabel.setText("Total: " + departments.size());
            mostVisitedLabel.setText("Most Visited: " + departments.stream().max((a, b) -> a.getVisits() - b.getVisits()).get().getName());//76
         }

        @FXML private void onSearch() {
            loadDepartments(searchField.getText(), filterCombo.getValue());
        }

        @FXML private void onFilterChanged() {
            loadDepartments(searchField.getText(), filterCombo.getValue());
        }

        @FXML private void onAddDepartment() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminDept.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) searchField.getScene().getWindow();
                stage.setTitle("Add Departments");
                stage.setScene(new Scene(root, 1400,800));
                AddAdminDept addAdminDeptController = loader.getController();
                // If you have a DepartmentController instance, pass it, otherwise send null
                addAdminDeptController.setDepartmentController(null, null);

                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    public void loadFromService() {
        DepartmentService departmentService = new DepartmentService();
        departments = departmentService.getAllDepartments();
        updateAnalytics();
        loadDepartments("", "All");
    }


    public void setParentPane(AnchorPane parentPane) {
        this.parentPane = parentPane;
    }
    @FXML
    private void goBackToAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/adminpage.fxml"));
            AnchorPane adminView = loader.load();

            // Replace this pane with admin view
            parentPane.getChildren().setAll(adminView);

            AnchorPane.setTopAnchor(adminView, 0.0);
            AnchorPane.setBottomAnchor(adminView, 0.0);
            AnchorPane.setLeftAnchor(adminView, 0.0);
            AnchorPane.setRightAnchor(adminView, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}