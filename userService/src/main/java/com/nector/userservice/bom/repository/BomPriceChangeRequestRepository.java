package com.nector.userservice.bom.repository;

import com.nector.userservice.bom.entity.BomPriceChangeRequest;
import com.nector.userservice.bom.entity.BomPriceChangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BomPriceChangeRequestRepository extends JpaRepository<BomPriceChangeRequest, Long> {

    List<BomPriceChangeRequest> findByStatusOrderByRequestedAtDesc(BomPriceChangeStatus status);

    List<BomPriceChangeRequest> findByRawMaterialIdAndStatusOrderByRequestedAtDesc(Long rawMaterialId, BomPriceChangeStatus status);

    List<BomPriceChangeRequest> findByBomIdAndStatusOrderByRequestedAtDesc(Long bomId, BomPriceChangeStatus status);

    List<BomPriceChangeRequest> findAllByOrderByRequestedAtDesc();

    boolean existsByBomComponentIdAndStatus(Long bomComponentId, BomPriceChangeStatus status);
}
