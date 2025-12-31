package model;

public class Account {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role; // "admin" o "logged_user" o null
    
    public Account() {
        // Default è null (guest)
    }
    
    public Account(String email, String password, String firstName, String lastName, String role) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
    
    // Getters and Setters
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
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    // Utility methods per i nuovi ruoli
    public boolean isAdmin() {
        return "admin".equals(role);
    }
    
    public boolean isUser() {
        return "logged_user".equals(role);
    }
    
    public boolean isGuest() {
        return role == null || role.isEmpty();
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}