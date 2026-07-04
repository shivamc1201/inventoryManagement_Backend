package com.nector.userservice.model;

import com.nector.userservice.common.features.Features;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_feature_permissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "feature_id"}))
@Data
@EqualsAndHashCode(exclude = {})
@ToString(exclude = {})
public class RoleFeaturePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

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
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public RoleFeaturePermission() {}

    public RoleFeaturePermission(Long userId, Integer featureId, Features feature) {
        this.userId = userId;
        this.featureId = featureId;
        this.feature = feature;
    }

    public boolean hasPermission(String operation) {
        return switch (operation.toUpperCase()) {
            case "CREATE" -> canCreate;
            case "READ"   -> canRead;
            case "UPDATE" -> canUpdate;
            case "DELETE" -> canDelete;
            default       -> false;
        };
    }
}
