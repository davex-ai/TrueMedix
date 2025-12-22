package org.example.project.hospitalmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

public class Dashboard {

        @FXML
        private TilePane departmentTilePane;

        private final String[][] departments = {
                {"Cardiology", "/icons/heart.png"},
                {"Clinical", "/icons/stethoscope.png"},
                {"Dermatology", "/icons/skin.png"},
                {"Maternity", "/icons/baby.png"},
                {"Neurology", "/icons/brain.png"},
                {"Pediatric", "/icons/kid.png"}
        };

        @FXML
        public void initialize() {
            for (String[] dept : departments) {
                VBox card = createDepartmentCard(dept[0], dept[1]);
                departmentTilePane.getChildren().add(card);
            }
        }

        private VBox createDepartmentCard(String departmentName, String iconPath) {
            VBox card = new VBox();
            card.getStyleClass().add("department-card");
            card.setAlignment(Pos.CENTER);
            card.setSpacing(10);

            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitWidth(40);
            icon.setFitHeight(40);
            icon.getStyleClass().add("department-icon");

            Label label = new Label(departmentName);
            label.getStyleClass().add("department-label");

            card.getChildren().addAll(icon, label);

            card.setOnMouseClicked((MouseEvent e) -> {
                System.out.println("Clicked: " + departmentName);
            });

            return card;
        }
    }


