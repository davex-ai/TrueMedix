package org.example.project.hospitalmanagementsystem.model;
import java.time.LocalDate;
import java.util.List;

public class Department {
        private int id;
        private String name;
        private String description;
        private String operatingHours;
        private List<Integer> assignedDoctorIds;
        private int visits;
        private int availableSlots;
        private LocalDate createdDate;
        private boolean active;
        private List<String> services;


    public Department(int id, String name, String description, String operatingHours, int visits, int availableSlots, List<Integer> assignedDoctorIds, LocalDate createdDate,
                      boolean active, List<String> services ) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.operatingHours = operatingHours;
            this.visits = visits;
            this.availableSlots = availableSlots;
            this.assignedDoctorIds = assignedDoctorIds;
            this.createdDate = createdDate;
            this.active = active;
            this.services = services;
        }

        // Getters and Setters...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public List<Integer> getAssignedDoctorIds() {
        return assignedDoctorIds;
    }

    public void setAssignedDoctorIds(List<Integer> assignedDoctorIds) {
        this.assignedDoctorIds = assignedDoctorIds;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }

}


