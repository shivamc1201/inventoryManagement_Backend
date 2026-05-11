package com.nector.userservice.service;

import com.nector.userservice.dto.KpiMasterCreateRequest;
import com.nector.userservice.dto.KpiMasterResponse;
import com.nector.userservice.dto.KpiMasterUpdateRequest;
import com.nector.userservice.enums.KPICategory;
import com.nector.userservice.enums.KPIFrequency;
import com.nector.userservice.exception.KpiNotFoundException;
import com.nector.userservice.model.KpiMaster;
import com.nector.userservice.repository.KpiMasterRepository;
import com.nector.userservice.validator.KpiValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KpiMasterServiceImpl implements KpiMasterService {

    private final KpiMasterRepository kpiMasterRepository;
    private final KpiValidator kpiValidator;

    @Override
    @Transactional
    public KpiMasterResponse createKpi(KpiMasterCreateRequest request, Long createdBy) {
        log.info("Creating new KPI by user: {}", createdBy);
        
        KpiMaster kpi = new KpiMaster();
        kpi.setKpiName(request.getKpiName());
        kpi.setDescription(request.getDescription());
        kpi.setKpiCategory(request.getKpiCategory());
        kpi.setMeasurementUnit(request.getMeasurementUnit());
        kpi.setDefaultWeightage(request.getDefaultWeightage());
        kpi.setFrequency(request.getFrequency());
        kpi.setIsActive(request.getIsActive());
        kpi.setCreatedBy(createdBy);
        
        KpiMaster savedKpi = kpiMasterRepository.save(kpi);
        log.info("Created KPI with ID: {}", savedKpi.getId());
        
        return mapToResponse(savedKpi);
    }

    @Override
    @Transactional
    public KpiMasterResponse updateKpi(Long id, KpiMasterUpdateRequest request) {
        log.info("Updating KPI with ID: {}", id);
        
        KpiMaster kpi = kpiMasterRepository.findById(id)
                .orElseThrow(() -> new KpiNotFoundException(id));
        
        if (request.getKpiName() != null) {
            kpi.setKpiName(request.getKpiName());
        }
        if (request.getDescription() != null) {
            kpi.setDescription(request.getDescription());
        }
        if (request.getKpiCategory() != null) {
            kpi.setKpiCategory(request.getKpiCategory());
        }
        if (request.getMeasurementUnit() != null) {
            kpi.setMeasurementUnit(request.getMeasurementUnit());
        }
        if (request.getDefaultWeightage() != null) {
            kpi.setDefaultWeightage(request.getDefaultWeightage());
        }
        if (request.getFrequency() != null) {
            kpi.setFrequency(request.getFrequency());
        }
        if (request.getIsActive() != null) {
            kpi.setIsActive(request.getIsActive());
        }
        
        KpiMaster updatedKpi = kpiMasterRepository.save(kpi);
        log.info("Updated KPI with ID: {}", updatedKpi.getId());
        
        return mapToResponse(updatedKpi);
    }

    @Override
    @Transactional
    public void deleteKpi(Long id) {
        log.info("Soft-deleting KPI with ID: {}", id);
        
        KpiMaster kpi = kpiMasterRepository.findById(id)
                .orElseThrow(() -> new KpiNotFoundException(id));
        
        kpi.setIsActive(false);
        kpiMasterRepository.save(kpi);
        
        log.info("Soft-deleted KPI with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public KpiMasterResponse getKpiById(Long id) {
        KpiMaster kpi = kpiMasterRepository.findById(id)
                .orElseThrow(() -> new KpiNotFoundException(id));
        return mapToResponse(kpi);
    }

    @Override
    @Transactional(readOnly = true)
    public KpiMasterResponse getKpiByCode(String kpiCode) {
        KpiMaster kpi = kpiMasterRepository.findByKpiCode(kpiCode)
                .orElseThrow(() -> new KpiNotFoundException("KPI not found with code: " + kpiCode));
        return mapToResponse(kpi);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiMasterResponse> getAllActiveKpis() {
        return kpiMasterRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KpiMasterResponse> getAllActiveKpis(Pageable pageable) {
        return kpiMasterRepository.findByIsActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiMasterResponse> getKpisByCategory(KPICategory category) {
        return kpiMasterRepository.findActiveByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiMasterResponse> getKpisByFrequency(KPIFrequency frequency) {
        return kpiMasterRepository.findByFrequencyAndIsActiveTrue(frequency)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KpiMasterResponse> searchKpis(String search) {
        return kpiMasterRepository.searchActiveKpiMasters(search)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByKpiCode(String kpiCode) {
        return kpiMasterRepository.existsByKpiCode(kpiCode);
    }

    private KpiMasterResponse mapToResponse(KpiMaster kpi) {
        return KpiMasterResponse.builder()
                .id(kpi.getId())
                .kpiCode(kpi.getKpiCode())
                .kpiName(kpi.getKpiName())
                .description(kpi.getDescription())
                .kpiCategory(kpi.getKpiCategory())
                .measurementUnit(kpi.getMeasurementUnit())
                .defaultWeightage(kpi.getDefaultWeightage())
                .frequency(kpi.getFrequency())
                .isActive(kpi.getIsActive())
                .createdBy(kpi.getCreatedBy())
                .createdAt(kpi.getCreatedAt())
                .updatedAt(kpi.getUpdatedAt())
                .build();
    }
}
