package com.mybudgetbuddy.domain.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private UserStatus status;
    
    // Security
    private String passwordHash;
    private LocalDateTime lastLogin;
    private int failedLoginAttempts;
    private boolean isEmailVerified;
    private boolean isTwoFactorEnabled;
    
    // Profile
    private String profileImagePath;
    private String timeZone;
    private String locale;
    private String currency;
    
    // Metadata
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private String createdBy;
    
    public User() {
        this.id = UUID.randomUUID().toString();
        this.status = UserStatus.ACTIVE;
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.isEmailVerified = false;
        this.isTwoFactorEnabled = false;
        this.currency = "USD";
        this.timeZone = "UTC";
        this.locale = "en_US";
    }
    
    public User(String username, String email, String firstName, String lastName) {
        this();
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Business methods
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
    
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.lastModified = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        this.lastModified = LocalDateTime.now();
    }
    
    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.lastModified = LocalDateTime.now();
    }
    
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        this.lastModified = LocalDateTime.now();
    }
    
    public boolean isAccountLocked() {
        return failedLoginAttempts >= 5;
    }
    
    public void verifyEmail() {
        this.isEmailVerified = true;
        this.lastModified = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { 
        this.username = username;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { 
        this.firstName = firstName;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { 
        this.lastName = lastName;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { 
        this.dateOfBirth = dateOfBirth;
        this.lastModified = LocalDateTime.now();
    }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { 
        this.status = status;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    
    public boolean isEmailVerified() { return isEmailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.isEmailVerified = emailVerified; }
    
    public boolean isTwoFactorEnabled() { return isTwoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { 
        this.isTwoFactorEnabled = twoFactorEnabled;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { 
        this.profileImagePath = profileImagePath;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { 
        this.timeZone = timeZone;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getLocale() { return locale; }
    public void setLocale(String locale) { 
        this.locale = locale;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { 
        this.currency = currency;
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}

enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING_VERIFICATION,
    DELETED
}