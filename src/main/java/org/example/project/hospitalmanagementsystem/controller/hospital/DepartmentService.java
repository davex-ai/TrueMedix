package org.example.project.hospitalmanagementsystem.controller.hospital;

import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;
import org.example.project.hospitalmanagementsystem.model.Department;
import org.example.project.hospitalmanagementsystem.model.Doctor;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DepartmentService {

    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        String query = "SELECT * FROM department_stats";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String doctorIdsString = rs.getString("assigned_doctors");
                List<Integer> assignedDoctorIds = new ArrayList<>();
                if (doctorIdsString != null && !doctorIdsString.trim().isEmpty()) {
                    assignedDoctorIds = Arrays.stream(doctorIdsString.replaceAll("[\\[\\]]", "").split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                }

                String servicesStr = rs.getString("services");
                List<String> services = new ArrayList<>();
                if (servicesStr != null && !servicesStr.trim().isEmpty()) {
                    services = Arrays.stream(servicesStr.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                }

                Department dept = new Department(
                        rs.getInt("id"),
                        rs.getString("department"),
                        rs.getString("description"),
                        rs.getString("hours"),
                        rs.getInt("visits"),
                        rs.getInt("available_slots"),
                        assignedDoctorIds,
                        rs.getDate("created_date").toLocalDate(),
                        rs.getBoolean("status"),
                        services
                );
                list.add(dept);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Integer> getAppointmentCountByDepartment() {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT d.department, COUNT(a.appointment_id) AS appt_count " +
                "FROM doctor d " +
                "LEFT JOIN appointments a ON a.doctor_id = d.doctor_id " +
                "GROUP BY d.department";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("department"), rs.getInt("appt_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Integer> getDoctorCountByDepartment() {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT department, COUNT(doctor_id) AS doc_count FROM doctor GROUP BY department";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("department"), rs.getInt("doc_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean saveDepartment(Department dept) {
        String sql = "INSERT INTO department_stats (department, description, hours, visits, available_slots, created_date, status, assigned_doctors, services) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dept.getName());
            stmt.setString(2, dept.getDescription());
            stmt.setString(3, dept.getOperatingHours());
            stmt.setInt(4, dept.getVisits());
            stmt.setInt(5, dept.getAvailableSlots());
            stmt.setDate(6, java.sql.Date.valueOf(dept.getCreatedDate()));
            stmt.setBoolean(7, true);
            stmt.setString(8, dept.getAssignedDoctorIds().toString());
            stmt.setString(9, String.join(",", dept.getServices()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Doctor> getAllDoctorObjects() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT doctor_id, specialization, department, name, employment_date, salary, pending_appointments_count, status, bio, history, photo_path FROM doctor";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Doctor doc = new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("specialization"),
                        rs.getString("department"),
                        rs.getString("name"),
                        rs.getDate("employment_date").toLocalDate(),
                        rs.getDouble("salary"),
                        rs.getInt("pending_appointments_count"),
                        rs.getString("photo_path"),
                        Doctor.DoctorStatus.valueOf(rs.getString("status").toUpperCase()),
                        rs.getString("bio"),
                        rs.getString("history")
                );
                doctors.add(doc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    public List<Integer> getDoctorIdsByNames(List<String> doctorNames) {
        return getAllDoctorObjects().stream()
                .filter(doc -> doctorNames.contains(doc.getName()))
                .map(Doctor::getDoctorId)
                .collect(Collectors.toList());
    }

    public List<String> getAllDepartmentNames() {
        return getAllDepartments().stream()
                .map(Department::getName)
                .collect(Collectors.toList());
    }
}