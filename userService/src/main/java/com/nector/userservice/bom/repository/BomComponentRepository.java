package com.nector.userservice.bom.repository;

import com.nector.userservice.bom.entity.BomComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BomComponentRepository extends JpaRepository<BomComponent, Long> {

    List<BomComponent> findByBomId(Long bomId);

    void deleteByBomId(Long bomId);

    List<BomComponent> findByRawMaterialId(Long rawMaterialId);

    @Query("SELECT bc.bom.bomName FROM BomComponent bc WHERE bc.rawMaterialId = :rawMaterialId")
    List<String> findBomNamesByRawMaterialId(@Param("rawMaterialId") Long rawMaterialId);
}
