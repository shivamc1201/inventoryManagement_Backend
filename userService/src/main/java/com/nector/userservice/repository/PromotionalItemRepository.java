package com.nector.userservice.repository;

import com.nector.userservice.model.PromotionalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionalItemRepository extends JpaRepository<PromotionalItem, Long> {
    
    Optional<PromotionalItem> findByItemCode(String itemCode);
    
    List<PromotionalItem> findByActiveTrue();
    
    @Query("SELECT p FROM PromotionalItem p WHERE p.active = true AND p.id = :id")
    Optional<PromotionalItem> findActiveById(Long id);
    
    @Query("SELECT p FROM PromotionalItem p WHERE p.active = true AND p.quantity <= p.minimumThreshold")
    List<PromotionalItem> findLowStockItems();
    
    boolean existsByItemCode(String itemCode);
    
    boolean existsByUnitCode(String unitCode);
    
    Optional<PromotionalItem> findByUnitCode(String unitCode);
}
