package com.nector.userservice.interceptors.distributor.impl;

import com.nector.userservice.exception.ResourceNotFoundException;
import com.nector.userservice.interceptors.distributor.model.Distributor;
import com.nector.userservice.interceptors.distributor.model.DistributorRequestDTO;
import com.nector.userservice.interceptors.distributor.model.DistributorResponseDTO;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.interceptors.distributor.service.DistributorMapper;
import com.nector.userservice.interceptors.distributor.service.DistributorService;
import com.nector.userservice.ledger.dto.CreateLedgerAccountRequest;
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
@Transactional
public class DistributorServiceImpl implements DistributorService {
    
    private final DistributorRepository distributorRepository;
    private final DistributorMapper distributorMapper;
    private final LedgerAccountService ledgerAccountService;
    
    @Override
    public DistributorResponseDTO createDistributor(DistributorRequestDTO request) {
        log.info("Creating distributor with email: {}", request.getContactEmail());
        
        if (distributorRepository.existsByContactEmail(request.getContactEmail())) {
            throw new IllegalArgumentException("Distributor with email already exists: " + request.getContactEmail());
        }
        
        Distributor distributor = distributorMapper.toEntity(request);
        Distributor savedDistributor = distributorRepository.save(distributor);
        
        // Auto-create ledger account for the distributor
        try {
            CreateLedgerAccountRequest ledgerRequest = new CreateLedgerAccountRequest();
            ledgerRequest.setCompanyId(1L); // Default company ID
            ledgerRequest.setDistributorId(savedDistributor.getId());
            ledgerRequest.setAccountName(savedDistributor.getName() + " - Ledger Account");
            ledgerRequest.setCreditLimit(BigDecimal.ZERO);
            
            ledgerAccountService.createLedgerAccount(ledgerRequest, "system");
            log.info("Ledger account created for distributor: {}", savedDistributor.getId());
        } catch (Exception e) {
            log.warn("Failed to create ledger account for distributor: {}", savedDistributor.getId(), e);
        }
        
        log.info("Distributor created successfully with ID: {}", savedDistributor.getId());
        return distributorMapper.toResponseDTO(savedDistributor);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DistributorResponseDTO getDistributorById(Long id) {
        log.info("Fetching distributor with ID: {}", id);
        
        Distributor distributor = distributorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found with ID: " + id));
        
        return distributorMapper.toResponseDTO(distributor);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DistributorResponseDTO> getAllDistributors() {
        log.info("Fetching all distributors");
        
        return distributorRepository.findAll()
                .stream()
                .map(distributorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public DistributorResponseDTO updateDistributor(Long id, DistributorRequestDTO request) {
        log.info("Updating distributor with ID: {}", id);
        
        Distributor distributor = distributorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found with ID: " + id));
        
        // Check if email is being changed and if new email already exists
        if (!distributor.getContactEmail().equals(request.getContactEmail()) && 
            distributorRepository.existsByContactEmail(request.getContactEmail())) {
            throw new IllegalArgumentException("Distributor with email already exists: " + request.getContactEmail());
        }
        
        distributorMapper.updateEntity(distributor, request);
        Distributor updatedDistributor = distributorRepository.save(distributor);
        
        log.info("Distributor updated successfully with ID: {}", updatedDistributor.getId());
        return distributorMapper.toResponseDTO(updatedDistributor);
    }
    
    @Override
    public void deleteDistributor(Long id) {
        log.info("Deleting distributor with ID: {}", id);
        
        if (!distributorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Distributor not found with ID: " + id);
        }
        
        distributorRepository.deleteById(id);
        log.info("Distributor deleted successfully with ID: {}", id);
    }
}