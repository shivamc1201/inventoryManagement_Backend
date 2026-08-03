package com.nector.userservice.interceptors.reports.repository;

import com.nector.userservice.interceptors.reports.entity.ProductionLogComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionLogComponentRepository extends JpaRepository<ProductionLogComponent, Long> {

    List<ProductionLogComponent> findByProductionLogId(Long productionLogId);

    void deleteByProductionLogId(Long productionLogId);
}
