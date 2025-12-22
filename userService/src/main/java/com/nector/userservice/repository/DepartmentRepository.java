package com.nector.userservice.repository;

import com.nector.userservice.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByActiveTrue();
    boolean existsByNameIgnoreCase(String name);
}