package org.example.project.hospitalmanagementsystem.model;
import javafx.beans.property.*;

import java.time.LocalDate;

public class Appointment {

    private final IntegerProperty appointmentId,doctorId;
    private final StringProperty name, email, status, notes;
    private final ObjectProperty<LocalDate> preferredDate;


    // Optionally, you can hold full user and doctor objects or their names separately if needed

    public Appointment(int appointmentId, String name, String email, int userId, int doctorId, LocalDate preferredDate, String status, String notes) {
        this.appointmentId = new SimpleIntegerProperty(appointmentId);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        IntegerProperty userId1 = new SimpleIntegerProperty(userId);
        this.doctorId = new SimpleIntegerProperty(doctorId);
        this.preferredDate = new SimpleObjectProperty<>(preferredDate);
        this.status = new SimpleStringProperty(status);
        this.notes = new SimpleStringProperty(notes);
    }

    // Getters
    public int getAppointmentId() { return appointmentId.get(); }
    public int getDoctorId() { return doctorId.get(); }
    public LocalDate getPreferredDate() { return preferredDate.get(); }
    public String getStatus() { return status.get(); }
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    // Properties for TableView bindings
    public IntegerProperty appointmentIdProperty() { return appointmentId; }
    public StringProperty statusProperty() { return status; }
    public StringProperty notesProperty() { return notes; }
}
