package com.nector.userservice.interceptors.reports.repository;

import com.nector.userservice.interceptors.reports.entity.ProductionLogComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionLogComponentRepository extends JpaRepository<ProductionLogComponent, Long> {

    List<ProductionLogComponent> findByProductionLogId(Long productionLogId);

    void deleteByProductionLogId(Long productionLogId);

    // [rawMaterialId, sum(quantityActual)] — total raw material consumed before a given date
    @Query("SELECT plc.rawMaterialId, SUM(plc.quantityActual) " +
           "FROM ProductionLogComponent plc " +
           "WHERE plc.productionLog.productionDate < :before " +
           "GROUP BY plc.rawMaterialId")
    List<Object[]> sumOutwardByMaterialBefore(@Param("before") LocalDate before);
}
