package edu.fiu.cen4021.elevate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @Column(name = "employee_id", length = 12, nullable = false)
    private String employeeId;

    @Column(name = "username", length = 80, nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", length = 100, nullable = false)
    private String passwordHash;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "role", length = 30, nullable = false)
    private String role;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected Employee() { }

    public Employee(String employeeId, String username, String passwordHash,
                    String fullName, String role, boolean enabled) {
        this.employeeId = employeeId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.enabled = enabled;
    }

    public String getEmployeeId() { return employeeId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public boolean isEnabled() { return enabled; }
}
