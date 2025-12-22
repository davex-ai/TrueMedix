package org.example.project.hospitalmanagementsystem.controller.admin;

import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;
import org.example.project.hospitalmanagementsystem.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.example.project.hospitalmanagementsystem.database.DatabaseConnection.getConnection;

public class DoctorDAO {
    public static List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctor LIMIT 30";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Doctor.DoctorStatus statusEnum;
                try {
                    statusEnum = Doctor.DoctorStatus.valueOf(rs.getString("status").toUpperCase());
                } catch (IllegalArgumentException e) {
                    statusEnum = Doctor.DoctorStatus.ACTIVE; // default if parsing fails
                }
                Doctor doctor = new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("specialization"),
                        rs.getString("department"),
                        rs.getString("name"),
                        rs.getDate("employment_date").toLocalDate(),
                        rs.getDouble("salary"),
                        rs.getInt("pending_appointments_count"),
                        rs.getString("photo_path"),
                        statusEnum,
                        rs.getString("bio"),
                        rs.getString("history")
                );

                doctors.add(doctor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return doctors;
    }

    public static boolean insertDoctor(Doctor doctor) {
        String sql = "INSERT INTO doctor (specialization, department, name, employment_date, salary, pending_appointments_count, photo_path, status, bio, history) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, doctor.getSpecialization());
            pstmt.setString(2, doctor.getDepartment());
            pstmt.setString(3, doctor.getName());
            pstmt.setDate(4, Date.valueOf(doctor.getEmploymentDate()));
            pstmt.setDouble(5, doctor.getSalary());
            pstmt.setInt(6, doctor.getPendingAppointmentsCount());
            pstmt.setString(7, doctor.getPhotoPath());
            pstmt.setString(8, doctor.getStatus().name());
            pstmt.setString(9, doctor.getBio());
            pstmt.setString(10, doctor.getHistory());

            System.out.println("Inserting doctor: " + doctor.getName());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteDoctorById(int id) {
        String sql = "DELETE FROM doctor WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Doctor> getDoctorsByDepartment(String departmentName) {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctor WHERE department = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departmentName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Doctor.DoctorStatus statusEnum;
                try {
                    statusEnum = Doctor.DoctorStatus.valueOf(rs.getString("status").toUpperCase());
                } catch (IllegalArgumentException e) {
                    statusEnum = Doctor.DoctorStatus.ACTIVE; // default if parsing fails
                }

                Doctor doctor = new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("specialization"),
                        rs.getString("department"),
                        rs.getString("name"),
                        rs.getDate("employment_date").toLocalDate(),
                        rs.getDouble("salary"),
                        rs.getInt("pending_appointments_count"),
                        rs.getString("photo_path"),
                        statusEnum,
                        rs.getString("bio"),
                        rs.getString("history")
                );
                doctors.add(doctor);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return doctors;
    }
    public static Integer getdepartmentByName(String name) {
        String query = "SELECT id FROM department_stats WHERE department = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) { 

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // depart not found
    }


}
