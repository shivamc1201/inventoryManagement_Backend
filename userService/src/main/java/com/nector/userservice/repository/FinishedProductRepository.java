package com.nector.userservice.repository;

import com.nector.userservice.model.FinishedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinishedProductRepository extends JpaRepository<FinishedProduct, Long> {
    
    Optional<FinishedProduct> findBySku(String sku);
    
    List<FinishedProduct> findByActiveTrue();
    
    @Query("SELECT fp FROM FinishedProduct fp WHERE fp.active = true AND fp.id = :id")
    Optional<FinishedProduct> findActiveById(Long id);
    
    @Query("SELECT fp FROM FinishedProduct fp WHERE fp.active = true AND fp.quantity <= fp.minimumThreshold")
    List<FinishedProduct> findLowStockItems();
    
    boolean existsBySku(String sku);
}