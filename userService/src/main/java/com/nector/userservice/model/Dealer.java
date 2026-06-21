package com.nector.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dealers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dealer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distributor_id")
    private Long distributorId;

    @Column(name = "salesperson_id")
    private Long salespersonId;

    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    @Column(name = "full_name")
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Size(max = 300, message = "Address cannot exceed 300 characters")
    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
    }
}
