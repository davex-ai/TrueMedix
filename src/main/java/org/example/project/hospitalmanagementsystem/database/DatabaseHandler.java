package org.example.project.hospitalmanagementsystem.database;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.example.project.hospitalmanagementsystem.controller.admin.AppointmentDAO;
import org.example.project.hospitalmanagementsystem.controller.admin.ComplaintDAO;
import org.example.project.hospitalmanagementsystem.model.Appointment;
import org.example.project.hospitalmanagementsystem.model.Complaint;
import org.example.project.hospitalmanagementsystem.model.User;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.example.project.hospitalmanagementsystem.database.DatabaseConnection.getConnection;

public class DatabaseHandler {
    @FXML private ComboBox<String> timeComboBox;

    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                //  User is a class with a constructor that accepts these fields
                return new User(
                        rs.getInt("id"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("remember_me")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // Return null if no user is found
    }

    public boolean updateUser(int id, String password, String role) {
        String query = "UPDATE users SET password = ?, role = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, password);
            stmt.setString(2, role);
            stmt.setInt(3, id);

            int result = stmt.executeUpdate();
            return result > 0;  // Returns true if the update was successful

        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Returns false if there's an error
        }
    }

    public boolean deleteUser(int id) {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            int result = stmt.executeUpdate();
            return result > 0;  // Returns true if the deletion was successful

        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Returns false if there's an error
        }
    }


    public static String convertTo24Hour(String time12h) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("h:mm a");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime time = LocalTime.parse(time12h.toUpperCase(), inputFormatter);
            return time.format(outputFormatter); // returns "11:00:00"
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null; // or handle error
        }
    }
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    // Calls DAO methods
    public ObservableList<Appointment> loadAppointments() {
        return appointmentDAO.getAllAppointments();
    }


    public boolean cancelAppointment(String email, String time) {
        return appointmentDAO.deleteAppointment(email, time);
    }
    private static DatabaseHandler instance;

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }
    public static Integer getUserIdByEmail(String email) {
        String query = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // user not found
    }

    public static Integer getdoctorIdByName(String name) {
        String query = "SELECT doctor_id FROM doctor WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("doctor_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // user not found
    }
    public ResultSet execQuery(String query) {
        ResultSet result = null;
        try {
            Statement stmt = getConnection().createStatement();
            result = stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void sendNotification(String email, String s){

    }

}


