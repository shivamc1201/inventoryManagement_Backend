package com.nector.userservice.stock.repository;

import com.nector.userservice.stock.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    // [itemType, itemCode, itemName, unit, totalInward, totalOutward]
    @Query("""
        SELECT s.itemType, s.itemCode, s.itemName, s.unit,
               SUM(CASE WHEN s.txnType = 'INWARD'  THEN s.quantity ELSE 0 END),
               SUM(CASE WHEN s.txnType = 'OUTWARD' THEN s.quantity ELSE 0 END)
        FROM StockTransaction s
        WHERE s.txnDate >= :fromDate AND s.txnDate <= :toDate
        GROUP BY s.itemType, s.itemCode, s.itemName, s.unit
        ORDER BY s.itemType, s.itemName
        """)
    List<Object[]> summariseByItem(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}
