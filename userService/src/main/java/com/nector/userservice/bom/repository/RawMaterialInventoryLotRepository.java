package com.nector.userservice.bom.repository;

import com.nector.userservice.bom.entity.RawMaterialInventoryLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface RawMaterialInventoryLotRepository extends JpaRepository<RawMaterialInventoryLot, Long> {

    List<RawMaterialInventoryLot> findByRawMaterialIdAndQuantityRemainingGreaterThanOrderByReceivedAtAsc(
            Long rawMaterialId, BigDecimal zero);

    // --- Report queries ---

    @Query("SELECT l FROM RawMaterialInventoryLot l WHERE l.receivedAt BETWEEN :from AND :to ORDER BY l.receivedAt DESC")
    List<RawMaterialInventoryLot> findByReceivedAtBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT l FROM RawMaterialInventoryLot l WHERE l.rawMaterialId = :rawMaterialId " +
           "AND l.receivedAt BETWEEN :from AND :to ORDER BY l.receivedAt ASC")
    List<RawMaterialInventoryLot> findByRawMaterialIdAndDateRange(@Param("rawMaterialId") Long rawMaterialId,
                                                                   @Param("from") Instant from,
                                                                   @Param("to") Instant to);
}
