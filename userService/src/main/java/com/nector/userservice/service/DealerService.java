package com.nector.userservice.service;

import com.nector.userservice.dto.DealerCreateRequest;
import com.nector.userservice.dto.DealerUpdateRequest;
import com.nector.userservice.dto.DealerResponse;
import com.nector.userservice.exception.BusinessException;
import com.nector.userservice.interceptors.distributor.model.Distributor;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.model.Dealer;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.repository.DealerRepository;
import com.nector.userservice.repository.SalesPersonRepository;
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
public class DealerService {

    private final DealerRepository dealerRepository;
    private final DealerLedgerService dealerLedgerService;
    private final DistributorRepository distributorRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final EmployeeKpiAssignmentService employeeKpiAssignmentService;

    @Transactional
    public DealerResponse createDealer(DealerCreateRequest request, Long distributorId, Long salespersonId) {
        log.info("Creating dealer for distributor: {} with salesperson: {}", distributorId, salespersonId);

        // Check if phone already exists for this distributor
        if (dealerRepository.existsByPhoneAndDistributorId(request.getPhone(), distributorId)) {
            throw new BusinessException("Phone number already exists for this distributor");
        }

        // Validate salesperson if provided
        if (salespersonId != null) {
            SalesPerson salesPerson = salesPersonRepository.findById(salespersonId)
                    .orElseThrow(() -> new BusinessException("Salesperson not found"));
            
            if (!salesPerson.getActive()) {
                throw new BusinessException("Salesperson is not active");
            }
        }

        Dealer dealer = new Dealer();
        dealer.setDistributorId(distributorId);
        dealer.setSalespersonId(salespersonId);
        dealer.setFullName(request.getFullName());
        dealer.setPhone(request.getPhone());
        dealer.setAddress(request.getAddress());
        dealer.setIsActive(true);

        Dealer savedDealer = dealerRepository.save(dealer);
        log.info("Created dealer with ID: {}", savedDealer.getId());

        // Auto-update Dealer Onboard KPI for the salesperson
        try {
            if (salespersonId != null) {
                employeeKpiAssignmentService.autoUpdateKpiAchieved(
                        salespersonId,
                        "Dealer Onboard",
                        java.math.BigDecimal.ONE
                );
                log.info("Auto-updated 'Dealer Onboard' KPI for salesperson {}", salespersonId);
            }
        } catch (Exception e) {
            log.warn("Failed to auto-update 'Dealer Onboard' KPI for salesperson {}: {}", salespersonId, e.getMessage());
        }

        // Auto-initialize opening balance for the dealer
        BigDecimal openingBalance = request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO;
        dealerLedgerService.initializeOpeningBalance(savedDealer.getId(), distributorId, openingBalance);
        log.info("Initialized opening balance for dealer: {} with amount: {}", savedDealer.getId(), openingBalance);

        return convertToResponse(savedDealer);
    }

    @Transactional(readOnly = true)
    public List<DealerResponse> getActiveDealers(Long distributorId, String search) {
        log.info("Fetching active dealers for distributor: {}", distributorId);

        List<Dealer> dealers;
        if (search != null && !search.trim().isEmpty()) {
            dealers = dealerRepository.searchActiveDealers(distributorId, search.trim());
        } else {
            dealers = dealerRepository.findByDistributorIdAndIsActiveTrue(distributorId);
        }

        return dealers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DealerResponse> getDealersByDistributorId(Long distributorId) {
        log.info("Fetching all dealers for distributor: {}", distributorId);

        List<Dealer> dealers = dealerRepository.findByDistributorIdAndIsActiveTrue(distributorId);

        return dealers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DealerResponse> getDealersBySalespersonId(Long salespersonId) {
        log.info("Fetching all dealers for salesperson: {}", salespersonId);

        // Validate salesperson exists and is active
        SalesPerson salesPerson = salesPersonRepository.findById(salespersonId)
                .orElseThrow(() -> new BusinessException("Salesperson not found"));
        
        if (!salesPerson.getActive()) {
            throw new BusinessException("Salesperson is not active");
        }

        List<Dealer> dealers = dealerRepository.findBySalespersonIdAndIsActiveTrue(salespersonId);

        return dealers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DealerResponse getDealerById(Long id, Long distributorId) {
        log.info("Fetching dealer by ID: {} for distributor: {}", id, distributorId);

        Dealer dealer = dealerRepository.findByIdAndDistributorId(id, distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        return convertToResponse(dealer);
    }

    @Transactional
    public DealerResponse updateDealer(Long id, DealerUpdateRequest request, Long distributorId) {
        log.info("Updating dealer with ID: {} for distributor: {}", id, distributorId);

        Dealer dealer = dealerRepository.findByIdAndDistributorId(id, distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        // Check if phone is being changed and if new phone already exists
        if (!dealer.getPhone().equals(request.getPhone()) &&
            dealerRepository.existsByPhoneAndDistributorId(request.getPhone(), distributorId)) {
            throw new BusinessException("Phone number already exists for this distributor");
        }

        dealer.setFullName(request.getFullName());
        dealer.setPhone(request.getPhone());
        dealer.setAddress(request.getAddress());
        if (request.getIsActive() != null) {
            dealer.setIsActive(request.getIsActive());
        }

        Dealer savedDealer = dealerRepository.save(dealer);
        log.info("Updated dealer with ID: {}", savedDealer.getId());

        return convertToResponse(savedDealer);
    }

    @Transactional
    public void softDeleteDealer(Long id, Long distributorId) {
        log.info("Soft deleting dealer with ID: {} for distributor: {}", id, distributorId);

        Dealer dealer = dealerRepository.findByIdAndDistributorId(id, distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        dealer.setIsActive(false);
        dealerRepository.save(dealer);
        log.info("Soft deleted dealer with ID: {}", id);
    }

    private DealerResponse convertToResponse(Dealer dealer) {
        DealerResponse response = new DealerResponse();
        response.setId(dealer.getId());
        response.setDistributorId(dealer.getDistributorId());
        response.setSalespersonId(dealer.getSalespersonId());
        
        // Fetch distributor name
        Distributor distributor = distributorRepository.findById(dealer.getDistributorId())
                .orElse(null);
        if (distributor != null) {
            response.setDistributorName(distributor.getFirstName() + " " + distributor.getLastName());
        }
        
        // Fetch salesperson name if assigned
        if (dealer.getSalespersonId() != null) {
            SalesPerson salesPerson = salesPersonRepository.findById(dealer.getSalespersonId())
                    .orElse(null);
            if (salesPerson != null) {
                response.setSalespersonName(salesPerson.getName());
            }
        }
        
        response.setFullName(dealer.getFullName());
        response.setPhone(dealer.getPhone());
        response.setAddress(dealer.getAddress());
        response.setIsActive(dealer.getIsActive());
        response.setCreatedAt(dealer.getCreatedAt());
        response.setUpdatedAt(dealer.getUpdatedAt());
        return response;
    }
}
