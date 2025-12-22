package org.example.project.hospitalmanagementsystem.controller.hospital;

import org.example.project.hospitalmanagementsystem.database.DatabaseConnection;
import org.example.project.hospitalmanagementsystem.model.Department;
import org.example.project.hospitalmanagementsystem.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DepartmentService {
            // Get all Departments from DB
            public List<Department> getAllDepartments() {
                List<Department> list = new ArrayList<>();
                String query = "SELECT * FROM department_stats";

                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    while (rs.next()) {
                        // 🟡 Step 1: Extract the assigned doctors string and convert to List<Integer>
                        String doctorIdsString = rs.getString("assigned_doctors");
                        List<Integer> assignedDoctorIds = new ArrayList<>();
                        if (doctorIdsString != null && !doctorIdsString.trim().isEmpty()) {
                            assignedDoctorIds = Arrays.stream(doctorIdsString.replaceAll("[\\[\\]]", "").split(","))
                                    .map(String::trim)
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());

                        }

                        // 🟢 Step 2: Extract services string and convert to List<String>
                        String servicesStr = rs.getString("services"); // e.g. "X-ray,Blood Test,Cardiology"
                        List<String> services = new ArrayList<>();
                        if (servicesStr != null && !servicesStr.trim().isEmpty()) {
                            services = Arrays.stream(servicesStr.split(","))
                                    .map(String::trim)
                                    .collect(Collectors.toList());
                        }

                        // 🟢 Step 2: Create the Department object using correct constructor
                        Department dept = new Department(
                                rs.getInt("id"),
                                rs.getString("department"), // make sure it's "name" in the table, or "department"
                                rs.getString("description"),
                                rs.getString("hours"),
                                rs.getInt("visits"),
                                rs.getInt("available_slots"),
                                assignedDoctorIds,
                                rs.getDate("created_date").toLocalDate(),
                                rs.getBoolean("status"),
                                services // e.g. "X-ray,Blood Test,Cardiology"
                        );
                        list.add(dept);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return list;
            }
    // Save a Department to DB
        public boolean saveDepartment(Department dept) {
            String sql = "INSERT INTO department_stats (department, description, hours, visits, available_slots, created_date, status, assigned_doctors, services ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, dept.getName());
                stmt.setString(2, dept.getDescription());
                stmt.setString(3, dept.getOperatingHours());
                stmt.setInt(4, dept.getVisits());
                stmt.setInt(5, dept.getAvailableSlots());
                stmt.setDate(6,java.sql.Date.valueOf(dept.getCreatedDate()));
                stmt.setBoolean(7,true);
                stmt.setString(8, dept.getAssignedDoctorIds().toString());// Storing as string for now
                stmt.setString(9, String.join(",", dept.getServices()));
                return stmt.executeUpdate() > 0;  //line74

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        // --- New method: get all doctors as Doctor objects ---
        public List<Doctor> getAllDoctorObjects() {
            List<Doctor> doctors = new ArrayList<>();
            String sql = "SELECT doctor_Id, specialization,  department, name, employment_date, salary, pending_appointments_count,status,  bio, history, photo_path FROM doctor";
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Doctor doc = new Doctor(
                            rs.getInt("doctor_Id"),
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

        // Now this method works correctly:
        public List<Integer> getDoctorIdsByNames(List<String> doctorNames) {
            List<Doctor> allDoctors = getAllDoctorObjects();  // Get full Doctor objects
            return allDoctors.stream()
                    .filter(doc -> doctorNames.contains(doc.getName()))  // filter by matching name
                    .map(Doctor::getDoctorId)  // map to IDs
                    .collect(Collectors.toList());
        }
    public List<String> getAllDepartmentNames() {
        List<Department> departments = getAllDepartments(); // assuming this method exists
        List<String> names = new ArrayList<>();
        for (Department dept : departments) {
            names.add(dept.getName()); // or getTitle(), depending on your class
        }
        return names;
    }

}
