package com.nector.userservice.bom.repository;

import com.nector.userservice.bom.entity.RawMaterialPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RawMaterialPriceHistoryRepository extends JpaRepository<RawMaterialPriceHistory, Long> {

    List<RawMaterialPriceHistory> findByRawMaterialIdOrderByChangedAtDesc(Long rawMaterialId);
}
