package com.example.scriptbooking.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class User implements Serializable {
    private long id;
    private String username;
    private String email;
    private String passwordHash;
    private String displayName;
    private String phone;
    private String status;
    private Set<String> roles = new LinkedHashSet<>();

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Set<String> getRoles() { return Collections.unmodifiableSet(roles); }
    public void setRoles(Set<String> roles) { this.roles = roles == null ? new LinkedHashSet<>() : new LinkedHashSet<>(roles); }
    public boolean hasRole(String role) { return roles.contains(role); }
    public boolean isManagement() { return hasRole("ADMIN") || hasRole("MANAGER") || hasRole("STAFF") || hasRole("VIEWER"); }
    public String getRoleText() { return String.join(" / ", roles); }

    /**
     * 登录后按角色进入各自的工作入口，避免所有管理角色都停留在同一个页面。
     */
    public String homePath() {
        if (hasRole("ADMIN") || hasRole("MANAGER") || hasRole("VIEWER")) return "/admin/dashboard";
        if (hasRole("STAFF")) return "/admin/bookings?status=CONFIRMED";
        return "/scripts";
    }
}
