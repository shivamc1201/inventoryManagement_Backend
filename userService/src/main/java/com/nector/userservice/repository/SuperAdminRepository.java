package com.nector.userservice.repository;

import com.nector.userservice.model.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {
    SuperAdmin findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}