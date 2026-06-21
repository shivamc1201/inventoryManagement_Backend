package com.nector.userservice.repository;

import com.nector.userservice.model.MachinePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MachinePartRepository extends JpaRepository<MachinePart, Long> {
    
    Optional<MachinePart> findByPartNumber(String partNumber);
    
    List<MachinePart> findByActiveTrue();
    
    List<MachinePart> findByCategory(MachinePart.Category category);
    
    List<MachinePart> findByCondition(MachinePart.Condition condition);
    
    @Query("SELECT mp FROM MachinePart mp WHERE mp.active = true AND mp.id = :id")
    Optional<MachinePart> findActiveById(Long id);
    
    boolean existsByPartNumber(String partNumber);
}