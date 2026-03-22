package com.nector.userservice.model;

import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.common.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_persons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic salesperson info
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SalesRole role;

    @Size(max = 100, message = "Zone cannot exceed 100 characters")
    @Column(length = 100)
    private String zone;

    @Size(max = 100, message = "Region cannot exceed 100 characters")
    @Column(length = 100)
    private String region;

    @Column(name = "manager_id")
    private Long managerId;

    // User account fields
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    // Contact information
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    @Column(name = "contact_no", length = 15)
    private String contactNo;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Alternate contact must be a valid 10-digit Indian mobile number")
    @Column(name = "alternate_contact_no", length = 15)
    private String alternateContactNo;

    @Size(max = 10, message = "Blood group cannot exceed 10 characters")
    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    @Column(name = "complete_address", columnDefinition = "TEXT")
    private String completeAddress;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 10, message = "Gender cannot exceed 10 characters")
    @Column(length = 10)
    private String gender;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Size(max = 20, message = "ZIP code cannot exceed 20 characters")
    private String zip;

    // Email (for both salesperson and user)
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Column(length = 255)
    private String email;

    @Size(max = 50, message = "Employee roll number cannot exceed 50 characters")
    @Column(name = "employee_roll_no", length = 50)
    private String employeeRollNo;

    // Legacy phone field for backward compatibility
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    @Column(length = 15)
    private String phone;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
        // Set phone to contactNo if phone is null
        if (phone == null && contactNo != null) {
            phone = contactNo;
        }
        // Set name to firstName + lastName if name is null
        if (name == null && firstName != null && lastName != null) {
            name = firstName + " " + lastName;
        }
    }
}
