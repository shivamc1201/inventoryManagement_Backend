package com.nector.userservice.repository;

import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByFeature(Features feature);
}