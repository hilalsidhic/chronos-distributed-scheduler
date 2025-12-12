package com.hilal.Chronos_Gateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String roles;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
}