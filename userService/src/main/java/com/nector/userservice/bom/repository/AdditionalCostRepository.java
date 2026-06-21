package com.nector.userservice.bom.repository;

import com.nector.userservice.bom.entity.AdditionalCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdditionalCostRepository extends JpaRepository<AdditionalCost, Long> {

    List<AdditionalCost> findByBomId(Long bomId);

    void deleteByBomId(Long bomId);
}
