package org.example.project.hospitalmanagementsystem.model;
import javafx.beans.property.*;
import javafx.scene.image.Image;
public class DepartmentModel {
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty description = new SimpleStringProperty();
        private final StringProperty imagePath = new SimpleStringProperty();
        private final StringProperty operatingHours = new SimpleStringProperty();
        private final IntegerProperty visits = new SimpleIntegerProperty();
        private final IntegerProperty availableSlots = new SimpleIntegerProperty();
        private final ObjectProperty<Image> image = new SimpleObjectProperty<>();


        public DepartmentModel(Department dept) {
            this.id.set(dept.getId());
            this.name.set(dept.getName());
            this.description.set(dept.getDescription());
            this.operatingHours.set(dept.getOperatingHours());
            this.visits.set(dept.getVisits());
            this.availableSlots.set(dept.getAvailableSlots());

        }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public String getImagePath() {
        return imagePath.get();
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }

    public String getOperatingHours() {
        return operatingHours.get();
    }

    public StringProperty operatingHoursProperty() {
        return operatingHours;
    }

    public int getVisits() {
        return visits.get();
    }

    public IntegerProperty visitsProperty() {
        return visits;
    }

    public int getAvailableSlots() {
        return availableSlots.get();
    }

    public IntegerProperty availableSlotsProperty() {
        return availableSlots;
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    // Getters: getName(), nameProperty()... etc.
    }


