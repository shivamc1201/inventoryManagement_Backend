package com.nector.userservice.bom.repository;

import com.nector.userservice.bom.entity.RawMaterialInventoryLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RawMaterialInventoryLotRepository extends JpaRepository<RawMaterialInventoryLot, Long> {

    List<RawMaterialInventoryLot> findByRawMaterialIdAndQuantityRemainingGreaterThanOrderByReceivedAtAsc(
            Long rawMaterialId, BigDecimal zero);
}
