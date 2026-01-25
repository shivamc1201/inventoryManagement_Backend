package com.nector.userservice.interceptors.salesMapping.repository;

import com.nector.userservice.interceptors.salesMapping.model.SalespersonDistributorMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesMappingRepository extends JpaRepository<SalespersonDistributorMapping, Long> {
    boolean existsBySalespersonIdAndDistributorId(Long salespersonId, Long distributorId);
    List<SalespersonDistributorMapping> findBySalespersonId(Long salespersonId);
    List<SalespersonDistributorMapping> findByDistributorId(Long distributorId);
    Optional<SalespersonDistributorMapping> findBySalespersonIdAndDistributorIdAndStatus(Long salespersonId, Long distributorId, com.nector.userservice.interceptors.salesMapping.model.MappingStatus status);
}