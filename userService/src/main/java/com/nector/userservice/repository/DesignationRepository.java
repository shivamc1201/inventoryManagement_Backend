package com.nector.userservice.repository;

import com.nector.userservice.model.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    List<Designation> findByActiveTrue();
    List<Designation> findByDepartmentIdAndActiveTrue(Long departmentId);
    boolean existsByNameIgnoreCase(String name);
}