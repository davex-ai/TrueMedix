package org.example.project.hospitalmanagementsystem.model;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.Period;
import java.util.concurrent.ThreadLocalRandom;

public class Doctor {

    private int doctorId;
    private String name;
    private LocalDate employmentDate;
    private String specialization;
    private String department;
    private double salary;
    private int pendingAppointmentsCount;
    private String photoPath; // Relative path like "doctors/doctor1.png"
    private DoctorStatus status;
    private String bio;
    private String history;





    public enum DoctorStatus {
        ACTIVE, ON_LEAVE, RETIRED
    }

    // Constructor
    public Doctor(int doctorId, String specialization, String department, String name, LocalDate employmentDate,
                  double salary, int pendingAppointmentsCount, String photoPath, DoctorStatus status, String bio, String history) {
        this.doctorId = doctorId;
        this.specialization = specialization;
        this.department = department;
        this.name = name;
        this.employmentDate = employmentDate;
        this.salary = salary;
        this.pendingAppointmentsCount = pendingAppointmentsCount;
        this.photoPath = photoPath;
        this.status = status != null ? status : DoctorStatus.values()[ThreadLocalRandom.current().nextInt(3)];
        this.bio = bio;
        this.history = history;

    }

    // Getters and Setters
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public LocalDate getEmploymentDate() { return employmentDate; }

    public void setEmploymentDate(LocalDate employmentDate) { this.employmentDate = employmentDate; }

    public String getSpecialization() { return specialization; }

    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getDepartment() { return department; }

    public void setDepartment(String department) { this.department = department; }

    public double getSalary() { return salary; }

    public void setSalary(double salary) { this.salary = salary; }

    public int getPendingAppointmentsCount() { return pendingAppointmentsCount; }

    public void setPendingAppointmentsCount(int count) { this.pendingAppointmentsCount = count; }

    public String getPhotoPath() { return photoPath; }

    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public DoctorStatus getStatus() { return status; }

    public void setStatus(DoctorStatus status) { this.status = status; }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public Image getPhoto() {
        String path = (photoPath != null && !photoPath.isEmpty()) ? "/media/" + photoPath : "/media/doctors/maledoc1.jpg";

        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream != null) {
                return new Image(stream);
            } else {
                System.err.println("Image not found: " + path);
                return new Image(getClass().getResourceAsStream("/media/avatar.png"));
            }
        } catch (Exception e) {
            System.err.println("Error loading photo: " + path);
            e.printStackTrace();
            return new Image(getClass().getResourceAsStream("/media/avatar.png"));
        }
    }
    public int getExperienceYears() {
        if (employmentDate == null) {
            return 0;
        }
        return Period.between(employmentDate, LocalDate.now()).getYears();
    }
}
