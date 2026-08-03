package com.nector.userservice.interceptors.reports.repository;

import com.nector.userservice.interceptors.reports.entity.ProductionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {

    Page<ProductionLog> findByProductionDateBetweenOrderByProductionDateDesc(LocalDate from, LocalDate to, Pageable pageable);

    List<ProductionLog> findByFinishedProductIdOrderByProductionDateDesc(Long finishedProductId);

    boolean existsByProductionNumber(String productionNumber);

    @Query("SELECT pl.finishedProductId, pl.finishedProductName, COUNT(pl), SUM(pl.quantityProduced), SUM(pl.totalProductionCost) " +
           "FROM ProductionLog pl WHERE pl.productionDate BETWEEN :from AND :to " +
           "GROUP BY pl.finishedProductId, pl.finishedProductName ORDER BY SUM(pl.quantityProduced) DESC")
    List<Object[]> getProductionSummaryByProduct(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT pl FROM ProductionLog pl WHERE pl.finishedProductId = :productId " +
           "AND pl.productionDate BETWEEN :from AND :to ORDER BY pl.productionDate DESC")
    List<ProductionLog> findByProductAndDateRange(@Param("productId") Long productId,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);
}
