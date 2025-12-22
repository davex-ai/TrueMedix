package org.example.project.hospitalmanagementsystem.controller.admin;

import com.mysql.cj.jdbc.DatabaseMetaData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;
import org.example.project.hospitalmanagementsystem.model.Complaint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {
    public boolean insertComplaint(String userEmail,String name, String category, String urgency, String phone, String message, String screenshotPath) {
        String sql = "INSERT INTO complaints (user_email, Name, category, urgency, phone, message, screenshot_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,userEmail);
            stmt.setString(2,name);
            stmt.setString(3, category);
            stmt.setString(4, urgency);
            stmt.setString(5, phone);
            stmt.setString(6, message);
            stmt.setString(7, screenshotPath);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<Complaint> getAllComplaints() {
        ObservableList<Complaint> list = FXCollections.observableArrayList();
        String sql = "SELECT complaint_id, Name,user_email, category, urgency, phone, message,screenshot_path, status, responded_at, response, submitted_at FROM complaints";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Complaint(
                        rs.getInt("complaint_id"),
                        rs.getString("Name"),
                        rs.getString("user_email"),
                        rs.getString("category"),
                        rs.getString("urgency"),
                        rs.getString("phone"),
                        rs.getString("message"),
                        rs.getString("screenshot_path"),
                        rs.getString("status"),
                        rs.getString("submitted_at"),
                        rs.getString("responded_at"),
                        rs.getString("response")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public boolean respondToComplaint(int complaintId, String response) {
        String sql = "UPDATE complaints SET status = 'Resolved', response = ?, responded_at = CURRENT_TIMESTAMP WHERE complaint_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, response);
            stmt.setInt(2, complaintId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteComplaint(int complaintId) {
        String sql = "DELETE FROM complaints WHERE complaint_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, complaintId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<String> getResponsesForUser(String email) {
        List<String> responses = new ArrayList<>();
        String sql = "SELECT response FROM complaints WHERE user_email = ? AND status = 'responded'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String response = rs.getString("response");
                responses.add("Admin responded to your complaint: " + response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return responses;
    }


}
