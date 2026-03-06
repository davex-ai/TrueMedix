package org.example.project.hospitalmanagementsystem.controller.users;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.project.hospitalmanagementsystem.controller.hospital.DepartmentService;
import org.example.project.hospitalmanagementsystem.model.Department;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DepartmentCard implements Initializable {

    @FXML private TilePane departmentTilePane;

    private final DepartmentService departmentService = new DepartmentService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDepartmentCards();
    }

    private void loadDepartmentCards() {
        List<Department> departments = departmentService.getAllDepartments();
        departmentTilePane.getChildren().clear();
        for (Department dept : departments)
            departmentTilePane.getChildren().add(createDepartmentCard(dept));
    }

    public void addDepartmentCard(Department department) {
        VBox card = createDepartmentCard(department);
        departmentTilePane.getChildren().add(card);
    }

    private VBox createDepartmentCard(Department department) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setPrefSize(270, 160);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 3); -fx-cursor: hand;"
        );

        Label statusDot = new Label(department.isActive() ? "● Active" : "● Inactive");
        statusDot.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';"
                + (department.isActive() ? "-fx-text-fill: #1a7a45;" : "-fx-text-fill: #b03030;"));

        Label name = new Label(department.getName());
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1a2e4a; -fx-font-family: 'Segoe UI';");
        name.setWrapText(true);

        Label desc = new Label(department.getDescription());
        desc.setWrapText(true);
        desc.setMaxHeight(36);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #7a8fa6; -fx-font-family: 'Segoe UI';");

        Label meta = new Label("👨‍⚕️ " + department.getAssignedDoctorIds().size() + " doctors  🗓 " + department.getAvailableSlots() + " slots");
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #3d5a73; -fx-font-family: 'Segoe UI';");

        card.getChildren().addAll(statusDot, name, desc, meta);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #eaf2fb; -fx-background-radius: 12; -fx-border-radius: 12;" +
                        "-fx-border-color: #1a6fbd; -fx-border-width: 1.5;" +
                        "-fx-effect: dropshadow(gaussian, rgba(26,111,189,0.18), 10, 0, 0, 4); -fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 3); -fx-cursor: hand;"
        ));

        card.setOnMouseClicked(e -> openDepartmentView(department));
        return card;
    }

    private void openDepartmentView(Department department) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/DepartmentView.fxml"));
            Parent root = loader.load();

            DepartmentView controller = loader.getController();
            controller.setDepartment(department);

            Stage stage = (Stage) departmentTilePane.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle(department.getName() + " Department");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        NavHelper.goHome((Node) event.getSource());
    }
}