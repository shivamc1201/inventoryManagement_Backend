package com.nector.userservice.repository;

import com.nector.userservice.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Sale s WHERE s.saleDate >= :startDate AND s.saleDate <= :endDate")
    BigDecimal getTotalSalesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate >= :startDate AND s.saleDate <= :endDate")
    Long getTransactionCountBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s.region, COALESCE(SUM(s.amount), 0) FROM Sale s WHERE s.saleDate >= :startDate AND s.saleDate <= :endDate GROUP BY s.region")
    List<Object[]> getSalesByRegion(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s.productCategory, COALESCE(SUM(s.amount), 0) FROM Sale s WHERE s.saleDate >= :startDate AND s.saleDate <= :endDate GROUP BY s.productCategory")
    List<Object[]> getSalesByCategory(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}