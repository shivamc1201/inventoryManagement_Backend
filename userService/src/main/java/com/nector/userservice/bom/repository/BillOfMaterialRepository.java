package com.nector.userservice.bom.repository;

import com.nector.userservice.bom.entity.BillOfMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, Long> {

    Optional<BillOfMaterial> findByBomNameIgnoreCase(String bomName);

    Page<BillOfMaterial> findByBomNameContainingIgnoreCaseOrFinishedProductNameContainingIgnoreCase(
            String bom, String product, Pageable pageable);

    boolean existsByBomNameIgnoreCase(String bomName);
}
