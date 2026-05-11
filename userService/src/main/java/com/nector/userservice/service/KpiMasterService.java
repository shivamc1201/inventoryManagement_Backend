package com.nector.userservice.service;

import com.nector.userservice.dto.KpiMasterCreateRequest;
import com.nector.userservice.dto.KpiMasterResponse;
import com.nector.userservice.dto.KpiMasterUpdateRequest;
import com.nector.userservice.enums.KPICategory;
import com.nector.userservice.enums.KPIFrequency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface KpiMasterService {

    KpiMasterResponse createKpi(KpiMasterCreateRequest request, Long createdBy);

    KpiMasterResponse updateKpi(Long id, KpiMasterUpdateRequest request);

    void deleteKpi(Long id);

    KpiMasterResponse getKpiById(Long id);

    KpiMasterResponse getKpiByCode(String kpiCode);

    List<KpiMasterResponse> getAllActiveKpis();

    Page<KpiMasterResponse> getAllActiveKpis(Pageable pageable);

    List<KpiMasterResponse> getKpisByCategory(KPICategory category);

    List<KpiMasterResponse> getKpisByFrequency(KPIFrequency frequency);

    List<KpiMasterResponse> searchKpis(String search);

    boolean existsByKpiCode(String kpiCode);
}
