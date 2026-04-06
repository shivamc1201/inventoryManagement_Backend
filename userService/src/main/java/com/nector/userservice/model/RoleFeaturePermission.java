package com.nector.userservice.model;

import com.nector.userservice.common.features.Features;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simplified role-feature permission entity using numeric IDs
 * Replaces the complex role-permission-feature relationship
 */
@Entity
@Table(name = "role_feature_permissions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "feature_id"}))
@Data
@EqualsAndHashCode(exclude = {})
@ToString(exclude = {})
public class RoleFeaturePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "role_id", nullable = false)
    private Integer roleId;
    
    @Column(name = "feature_id", nullable = false)
    private Integer featureId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "feature", nullable = false)
    private Features feature;
    
    @Column(nullable = false)
    private Boolean canCreate = false;
    
    @Column(nullable = false)
    private Boolean canRead = false;
    
    @Column(nullable = false)
    private Boolean canUpdate = false;
    
    @Column(nullable = false)
    private Boolean canDelete = false;
    
    @Column(name = "created_at")
    private Long createdAt;
    
    @Column(name = "updated_at")
    private Long updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
    
    public RoleFeaturePermission() {}
    
    public RoleFeaturePermission(Integer roleId, Integer featureId, Features feature) {
        this.roleId = roleId;
        this.featureId = featureId;
        this.feature = feature;
    }
    
    public boolean hasPermission(String operation) {
        return switch (operation.toUpperCase()) {
            case "CREATE" -> canCreate;
            case "READ" -> canRead;
            case "UPDATE" -> canUpdate;
            case "DELETE" -> canDelete;
            default -> false;
        };
    }
}
