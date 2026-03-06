package org.example.project.hospitalmanagementsystem.controller.users;

public class UserSession {

    private static UserSession instance;
    private String userEmail;
    private String userName;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public String  getUserEmail()         { return userEmail; }
    public void    setUserEmail(String e) { this.userEmail = e; }
    public String  getUserName()          { return userName; }
    public void    setUserName(String n)  { this.userName = n; }
    public boolean isLoggedIn()           { return userEmail != null && !userEmail.isEmpty(); }
    public void    clear()                { userEmail = null; userName = null; }
}