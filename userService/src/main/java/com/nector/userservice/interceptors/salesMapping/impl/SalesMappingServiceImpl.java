package com.nector.userservice.interceptors.salesMapping.impl;

import com.nector.userservice.exception.ResourceNotFoundException;
import com.nector.userservice.interceptors.distributor.model.Distributor;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.interceptors.salesMapping.model.*;
import com.nector.userservice.interceptors.salesMapping.repository.SalesMappingRepository;
import com.nector.userservice.interceptors.salesMapping.service.SalesMappingService;
import com.nector.userservice.ledger.dto.CreateLedgerAccountRequest;
import com.nector.userservice.ledger.entity.LedgerAccount;
import com.nector.userservice.ledger.service.LedgerAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesMappingServiceImpl implements SalesMappingService {
    
    private final SalesMappingRepository salesMappingRepository;
    private final UserRepository userRepository;
    private final DistributorRepository distributorRepository;
    private final LedgerAccountService ledgerAccountService;
    
    @Override
    @Transactional
    public MappingResponseDTO createMapping(CreateMappingRequestDTO request, String createdBy) {
        log.info("Creating mapping between salesperson {} and distributor {}", request.getSalespersonId(), request.getDistributorId());
        
        // Validate salesperson exists
        User salesperson = userRepository.findById(request.getSalespersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Salesperson not found with ID: " + request.getSalespersonId()));
        
        // Validate distributor exists
        Distributor distributor = distributorRepository.findById(request.getDistributorId())
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found with ID: " + request.getDistributorId()));
        
        // Check if mapping already exists
        if (salesMappingRepository.existsBySalespersonIdAndDistributorId(request.getSalespersonId(), request.getDistributorId())) {
            throw new IllegalArgumentException("Mapping already exists between salesperson and distributor");
        }
        
        // Check if ledger account already exists for distributor
        try {
            ledgerAccountService.getLedgerAccount(request.getCompanyId(), request.getDistributorId());
            throw new IllegalArgumentException("Ledger account already exists for distributor ID: " + request.getDistributorId());
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already exists")) {
                throw e; // Re-throw if it's our custom message
            }
            // Continue if ledger account not found (expected)
        }
        
        // Create mapping first
        SalespersonDistributorMapping mapping = new SalespersonDistributorMapping();
        mapping.setSalespersonId(request.getSalespersonId());
        mapping.setDistributorId(request.getDistributorId());
        mapping.setCompanyId(request.getCompanyId());
        mapping.setStatus(MappingStatus.ACTIVE);
        mapping.setCreatedBy(createdBy);
        
        SalespersonDistributorMapping savedMapping = salesMappingRepository.save(mapping);
        
        // Create new ledger account
        LedgerAccount ledgerAccount = createLedgerAccount(request.getCompanyId(), request.getDistributorId(), distributor.getName(), createdBy, request.getSalespersonId());
        
        log.info("Mapping created successfully with ID: {}", savedMapping.getId());
        return buildResponseDTO(savedMapping, salesperson, distributor, ledgerAccount);
    }
    
    private LedgerAccount createLedgerAccount(Long companyId, Long distributorId, String distributorName, String createdBy, Long salespersonId) {
        log.info("Creating new ledger account for distributor: {}", distributorId);
        try {
            CreateLedgerAccountRequest ledgerRequest = new CreateLedgerAccountRequest();
            ledgerRequest.setCompanyId(companyId);
            ledgerRequest.setDistributorId(distributorId);
            ledgerRequest.setSalespersonId(salespersonId);
            ledgerRequest.setAccountName(distributorName + " - Ledger Account");
            ledgerRequest.setCreditLimit(BigDecimal.ZERO);
            
            LedgerAccount created = ledgerAccountService.createLedgerAccount(ledgerRequest, createdBy);
            log.info("Ledger account created successfully with ID: {}", created.getId());
            return created;
        } catch (Exception ex) {
            log.error("Failed to create ledger account for distributor: {} - Error: {}", distributorId, ex.getMessage(), ex);
            throw new RuntimeException("Failed to create ledger account: " + ex.getMessage(), ex);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MappingResponseDTO> getMappingsBySalesperson(Long salespersonId) {
        return salesMappingRepository.findBySalespersonId(salespersonId)
                .stream()
                .map(this::buildResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MappingResponseDTO> getMappingsByDistributor(Long distributorId) {
        return salesMappingRepository.findByDistributorId(distributorId)
                .stream()
                .map(this::buildResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MappingResponseDTO> getAllMappings() {
        return salesMappingRepository.findAll()
                .stream()
                .map(this::buildResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deactivateMapping(Long mappingId) {
        SalespersonDistributorMapping mapping = salesMappingRepository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("Mapping not found with ID: " + mappingId));
        
        mapping.setStatus(MappingStatus.INACTIVE);
        salesMappingRepository.save(mapping);
        log.info("Mapping deactivated with ID: {}", mappingId);
    }
    
    private MappingResponseDTO buildResponseDTO(SalespersonDistributorMapping mapping) {
        User salesperson = userRepository.findById(mapping.getSalespersonId()).orElse(null);
        Distributor distributor = distributorRepository.findById(mapping.getDistributorId()).orElse(null);
        LedgerAccount ledgerAccount = null;
        
        try {
            ledgerAccount = ledgerAccountService.getLedgerAccount(mapping.getCompanyId(), mapping.getDistributorId());
        } catch (Exception e) {
            log.warn("Ledger account not found for distributor: {}", mapping.getDistributorId());
        }
        
        return buildResponseDTO(mapping, salesperson, distributor, ledgerAccount);
    }
    
    private MappingResponseDTO buildResponseDTO(SalespersonDistributorMapping mapping, User salesperson, Distributor distributor, LedgerAccount ledgerAccount) {
        MappingResponseDTO dto = new MappingResponseDTO();
        dto.setId(mapping.getId());
        dto.setSalespersonId(mapping.getSalespersonId());
        dto.setSalespersonName(salesperson != null ? salesperson.getFirstName() + " " + salesperson.getLastName() : "Unknown");
        dto.setDistributorId(mapping.getDistributorId());
        dto.setDistributorName(distributor != null ? distributor.getName() : "Unknown");
        dto.setCompanyId(mapping.getCompanyId());
        dto.setStatus(mapping.getStatus());
        dto.setLedgerAccountId(ledgerAccount != null ? ledgerAccount.getId() : null);
        dto.setCreatedOn(mapping.getCreatedOn());
        dto.setCreatedBy(mapping.getCreatedBy());
        return dto;
    }
}