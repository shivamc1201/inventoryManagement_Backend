package com.nector.userservice.repository;

import com.nector.userservice.model.HrMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HrMasterRepository extends JpaRepository<HrMaster, Long> {
    HrMaster findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}