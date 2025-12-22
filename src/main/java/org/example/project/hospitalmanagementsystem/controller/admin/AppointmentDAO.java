package org.example.project.hospitalmanagementsystem.controller.admin;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;
import org.example.project.hospitalmanagementsystem.model.Appointment;

import java.sql.*;
import java.time.LocalDate;

public class AppointmentDAO {
    public static boolean insertAppointment(int userId, int doctorId, String name, String email, String dept, String doc, Date date, String time, String notes) {
        String query = "INSERT INTO appointments (user_id, doctor_id, name, email, department, doctor, appointment_date, appointment_time, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, name);
            stmt.setString(4, email);
            stmt.setString(5, dept);
            stmt.setString(6, doc);
            stmt.setDate(7, new java.sql.Date(date.getTime()));
            stmt.setString(8, time);
            stmt.setString(9, notes);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public ObservableList<Appointment> getAllAppointments() {
        ObservableList<Appointment> list = FXCollections.observableArrayList();
        String query = "SELECT appointment_id, user_id, doctor_id, status, notes, name, email, appointment_date FROM appointments";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                list.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("user_id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getString("status"), rs.getString("notes")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    // DAO method
    public static boolean updateAppointmentStatus(String email, LocalDate date, String newStatus) {
        String query = "UPDATE appointments SET status=? WHERE email=? AND appointment_date=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus);
            stmt.setString(2, email);
            stmt.setDate(3, Date.valueOf(date)); // Convert LocalDate to java.sql.Date
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean approveAppointmentWithMessage(int appointmentId, String message) {
        String sql = "UPDATE appointments SET status = 'Approved', admin_message = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, message);
            stmt.setInt(2, appointmentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean deleteAppointment(String email, String time) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM appointments WHERE email=? AND appointment_time=?")) {
                stmt.setString(1, email);
                stmt.setString(2, time);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }


}


