package com.nector.userservice.repository;

import com.nector.userservice.model.ScrapItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapItemRepository extends JpaRepository<ScrapItem, Long> {
    
    Optional<ScrapItem> findByItemCode(String itemCode);
    
    List<ScrapItem> findByActiveTrue();
    
    @Query("SELECT s FROM ScrapItem s WHERE s.active = true AND s.id = :id")
    Optional<ScrapItem> findActiveById(Long id);
    
    @Query("SELECT s FROM ScrapItem s WHERE s.active = true AND s.quantity <= s.minimumThreshold")
    List<ScrapItem> findLowStockItems();
    
    boolean existsByItemCode(String itemCode);
    
    boolean existsByUnitCode(String unitCode);
    
    Optional<ScrapItem> findByUnitCode(String unitCode);
}
