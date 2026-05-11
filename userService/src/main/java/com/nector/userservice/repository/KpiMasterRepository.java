package com.nector.userservice.repository;

import com.nector.userservice.enums.KPICategory;
import com.nector.userservice.enums.KPIFrequency;
import com.nector.userservice.model.KpiMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KpiMasterRepository extends JpaRepository<KpiMaster, Long> {

    Optional<KpiMaster> findByKpiCode(String kpiCode);

    boolean existsByKpiCode(String kpiCode);

    List<KpiMaster> findByIsActiveTrue();

    List<KpiMaster> findByKpiCategoryAndIsActiveTrue(KPICategory category);

    List<KpiMaster> findByFrequencyAndIsActiveTrue(KPIFrequency frequency);

    Page<KpiMaster> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT k FROM KpiMaster k WHERE k.isActive = true AND " +
           "(LOWER(k.kpiCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(k.kpiName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(k.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<KpiMaster> searchActiveKpiMasters(@Param("search") String search);

    @Query("SELECT k FROM KpiMaster k WHERE k.isActive = true AND k.kpiCategory = :category")
    List<KpiMaster> findActiveByCategory(@Param("category") KPICategory category);

    @Query("SELECT COUNT(k) > 0 FROM KpiMaster k WHERE k.kpiCode = :kpiCode AND k.id != :id")
    boolean existsByKpiCodeAndIdNot(@Param("kpiCode") String kpiCode, @Param("id") Long id);
}
