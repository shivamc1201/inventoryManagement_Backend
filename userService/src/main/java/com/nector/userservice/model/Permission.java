package com.nector.userservice.model;

import com.nector.userservice.common.features.Features;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Data
@EqualsAndHashCode(exclude = "roles")
@ToString(exclude = "roles")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private Features feature;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column
    private String path;
    
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
    
    public Permission() {}
    
    public Permission(Features feature, String name, String description, String path) {
        this.feature = feature;
        this.name = name;
        this.description = description;
        this.path = path;
    }
}