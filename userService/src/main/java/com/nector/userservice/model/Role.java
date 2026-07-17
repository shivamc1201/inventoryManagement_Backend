package com.nector.userservice.model;

import com.nector.userservice.enums.RoleCategory;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(exclude = "users")
@ToString(exclude = "users")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_type", unique = true, nullable = false)
    private String roleType;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_category", nullable = false)
    private RoleCategory roleCategory;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    public Role() {}

    public Role(String roleType, String name, String description, RoleCategory roleCategory) {
        this.roleType = roleType;
        this.name = name;
        this.description = description;
        this.roleCategory = roleCategory;
    }
}
