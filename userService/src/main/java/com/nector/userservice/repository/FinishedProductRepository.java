package com.nector.userservice.repository;

import com.nector.userservice.model.FinishedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
    
    boolean existsByUnitCode(String unitCode);
    
    Optional<FinishedProduct> findByUnitCode(String unitCode);

    List<FinishedProduct> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    Optional<FinishedProduct> findByNameIgnoreCaseAndActiveTrue(String name);

    // --- Report queries ---

    @Query("SELECT COALESCE(SUM(fp.quantity * fp.price), 0) FROM FinishedProduct fp WHERE fp.active = true")
    BigDecimal getTotalFinishedProductValue();

    @Query("SELECT fp.batchNumber, COUNT(fp), SUM(fp.quantity) FROM FinishedProduct fp " +
           "WHERE fp.active = true AND fp.batchNumber IS NOT NULL " +
           "GROUP BY fp.batchNumber ORDER BY MAX(fp.createdAt) DESC")
    List<Object[]> getActiveBatchSummary();

    @Query("SELECT fp FROM FinishedProduct fp WHERE fp.expiryDate IS NOT NULL " +
           "AND fp.expiryDate <= :alertDate AND fp.active = true ORDER BY fp.expiryDate ASC")
    List<FinishedProduct> findExpiringBefore(@Param("alertDate") java.time.LocalDate alertDate);

    List<FinishedProduct> findByBatchNumberOrderByCreatedAtDesc(String batchNumber);
}