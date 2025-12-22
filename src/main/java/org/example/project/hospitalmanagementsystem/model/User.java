package org.example.project.hospitalmanagementsystem.model;

public class User {
    private int id;
    private String role;
    private String email;
    private String password;
    private boolean rememberMe;

    public User(int id, String role, String email, String password, boolean rememberMe) {
        this.id = id;
        this.role = role;
        this.email = email;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
