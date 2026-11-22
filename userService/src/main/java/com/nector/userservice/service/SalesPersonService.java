package com.nector.userservice.service;

import com.nector.userservice.dto.SalesPersonCreateRequest;
import com.nector.userservice.dto.SalesPersonResponse;
import com.nector.userservice.dto.SalesPersonUpdateRequest;
import com.nector.userservice.exception.BusinessException;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.repository.SalesPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SalesPersonService {

    private final SalesPersonRepository salesPersonRepository;
    private final SalesHierarchyValidationService validationService;

    public SalesPersonResponse createSalesPerson(SalesPersonCreateRequest request) throws BadRequestException {
        log.info("Creating salesperson with name: {}, role: {}, managerId: {}", 
                request.getName(), request.getRole(), request.getManagerId());

        validationService.validatePhoneUniqueness(request.getContactNo(), null);
        validationService.validateEmailUniqueness(request.getEmail(), null);
        validationService.validateUsernameUniqueness(request.getUsername(), null);
        
        // Validate employee roll number uniqueness if provided
        if (request.getEmployeeRollNo() != null && !request.getEmployeeRollNo().trim().isEmpty()) {
            validationService.validateEmployeeRollNoUniqueness(request.getEmployeeRollNo(), null);
        }
        
        // Handle National Sales Manager specially - skip manager validation entirely
        if (request.getRole() == com.nector.userservice.enums.SalesRole.NATIONAL_SALES_MGR) {
            log.info("Creating National Sales Manager - skipping manager validation");
            // Force managerId to null for National Sales Manager (handle 0, null, or any value)
            request.setManagerId(null);
        } else {
            // For other roles, validate manager assignment
            validationService.validateManagerAssignment(request.getRole(), request.getManagerId());
        }

        SalesPerson salesPerson = new SalesPerson();
        
        // Basic salesperson info
        salesPerson.setName(request.getName());
        salesPerson.setRole(request.getRole());
        salesPerson.setZone(request.getZone());
        salesPerson.setRegion(request.getRegion());
        salesPerson.setManagerId(request.getManagerId());
        
        // User account fields
        salesPerson.setUsername(request.getUsername());
        salesPerson.setPassword(request.getPassword()); // TODO: Add password encoding
        salesPerson.setStatus(request.getStatus());
        salesPerson.setFirstName(request.getFirstName());
        salesPerson.setLastName(request.getLastName());
        
        // Contact information
        salesPerson.setContactNo(request.getContactNo());
        salesPerson.setAlternateContactNo(request.getAlternateContactNo());
        salesPerson.setPhone(request.getContactNo()); // Set phone to contactNo for compatibility
        salesPerson.setEmail(request.getEmail());
        
        // Personal details
        salesPerson.setBloodGroup(request.getBloodGroup());
        salesPerson.setCompleteAddress(request.getCompleteAddress());
        salesPerson.setDateOfBirth(request.getDateOfBirth());
        salesPerson.setGender(request.getGender());
        salesPerson.setCity(request.getCity());
        salesPerson.setCountry(request.getCountry());
        salesPerson.setZip(request.getZip());
        
        // Work details
        salesPerson.setEmployeeRollNo(request.getEmployeeRollNo());

        SalesPerson saved = salesPersonRepository.save(salesPerson);
        log.info("Created salesperson with ID: {}", saved.getId());

        return convertToResponse(saved);
    }

    public SalesPersonResponse updateSalesPerson(Long id, SalesPersonUpdateRequest request) throws BadRequestException {
        log.info("Updating salesperson with ID: {}", id);

        SalesPerson existing = salesPersonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Salesperson not found"));

        if (!existing.getActive()) {
            throw new BusinessException("Cannot update inactive salesperson");
        }

        // Validate uniqueness if fields are being updated
        validationService.validatePhoneUniqueness(request.getContactNo() != null ? request.getContactNo() : request.getPhone(), id);
        validationService.validateEmailUniqueness(request.getEmail(), id);
        validationService.validateUsernameUniqueness(request.getUsername(), id);
        
        // Validate employee roll number uniqueness if provided
        if (request.getEmployeeRollNo() != null && !request.getEmployeeRollNo().trim().isEmpty()) {
            validationService.validateEmployeeRollNoUniqueness(request.getEmployeeRollNo(), id);
        }

        // Handle role and manager validation
        if (request.getRole() != null && request.getRole() != existing.getRole()) {
            // Handle National Sales Manager specially - skip manager validation entirely
            if (request.getRole() == com.nector.userservice.enums.SalesRole.NATIONAL_SALES_MGR) {
                log.info("Updating to National Sales Manager - setting managerId to null");
                existing.setManagerId(null);
            } else {
                // For other roles, validate manager assignment if managerId is being updated
                Long managerIdToValidate = request.getManagerId() != null ? request.getManagerId() : existing.getManagerId();
                validationService.validateManagerAssignment(request.getRole(), managerIdToValidate);
            }
        } else if (request.getManagerId() != null && !request.getManagerId().equals(existing.getManagerId())) {
            validationService.validateManagerAssignment(existing.getRole(), request.getManagerId());
        }

        // Basic salesperson info
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getRole() != null) {
            existing.setRole(request.getRole());
        }
        if (request.getZone() != null) {
            existing.setZone(request.getZone());
        }
        if (request.getRegion() != null) {
            existing.setRegion(request.getRegion());
        }
        if (request.getManagerId() != null) {
            existing.setManagerId(request.getManagerId());
        }
        
        // User account fields
        if (request.getUsername() != null) {
            existing.setUsername(request.getUsername());
        }
        if (request.getPassword() != null) {
            existing.setPassword(request.getPassword()); // TODO: Add password encoding
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getFirstName() != null) {
            existing.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            existing.setLastName(request.getLastName());
        }
        
        // Contact information
        if (request.getContactNo() != null) {
            existing.setContactNo(request.getContactNo());
            existing.setPhone(request.getContactNo()); // Set phone to contactNo for compatibility
        }
        if (request.getAlternateContactNo() != null) {
            existing.setAlternateContactNo(request.getAlternateContactNo());
        }
        if (request.getPhone() != null) {
            existing.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            existing.setEmail(request.getEmail());
        }
        
        // Personal details
        if (request.getBloodGroup() != null) {
            existing.setBloodGroup(request.getBloodGroup());
        }
        if (request.getCompleteAddress() != null) {
            existing.setCompleteAddress(request.getCompleteAddress());
        }
        if (request.getDateOfBirth() != null) {
            existing.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            existing.setGender(request.getGender());
        }
        if (request.getCity() != null) {
            existing.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            existing.setCountry(request.getCountry());
        }
        if (request.getZip() != null) {
            existing.setZip(request.getZip());
        }
        
        // Work details
        if (request.getEmployeeRollNo() != null) {
            existing.setEmployeeRollNo(request.getEmployeeRollNo());
        }

        SalesPerson saved = salesPersonRepository.save(existing);
        log.info("Updated salesperson with ID: {}", saved.getId());

        propagateChangesToOrders(saved);

        return convertToResponse(saved);
    }

    public void deleteSalesPerson(Long id) {
        log.info("Deleting salesperson with ID: {}", id);

        SalesPerson existing = salesPersonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Salesperson not found"));

        validationService.validateDeletion(existing);

        existing.setActive(false);
        salesPersonRepository.save(existing);
        log.info("Soft deleted salesperson with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<SalesPersonResponse> getAllSalesPersons() {
        List<SalesPerson> salesPersons = salesPersonRepository.findByActiveTrue();
        return salesPersons.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SalesPersonResponse getSalesPersonById(Long id) {
        SalesPerson salesPerson = salesPersonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Salesperson not found"));

        if (!salesPerson.getActive()) {
            throw new BusinessException("Salesperson not found");
        }

        return convertToResponse(salesPerson);
    }

    @Transactional(readOnly = true)
    public List<SalesPersonResponse> searchSalesPersons(String search) {
        List<SalesPerson> salesPersons = salesPersonRepository.searchActiveSalesPersons(search);
        return salesPersons.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SalesPersonResponse> getSalesPersonsByRole(com.nector.userservice.enums.SalesRole role) {
        log.info("Getting salespersons by role: {}", role);
        List<SalesPerson> salesPersons = salesPersonRepository.findActiveByRole(role);
        return salesPersons.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getSubordinateIds(Long salespersonId) {
        log.info("Getting subordinate IDs for salesperson ID: {}", salespersonId);
        
        // Get the salesperson
        SalesPerson salesPerson = salesPersonRepository.findById(salespersonId)
                .orElseThrow(() -> new BusinessException("Salesperson not found"));

        if (!salesPerson.getActive()) {
            throw new BusinessException("Salesperson not found");
        }

        // Get all subordinates recursively
        List<Long> subordinateIds = new java.util.ArrayList<>();
        getSubordinateIdsRecursive(salespersonId, subordinateIds);
        
        log.info("Found {} subordinates for salesperson ID: {}", subordinateIds.size(), salespersonId);
        return subordinateIds;
    }

    private void getSubordinateIdsRecursive(Long managerId, List<Long> subordinateIds) {
        // Get direct subordinates
        List<SalesPerson> directSubordinates = salesPersonRepository.findActiveByManagerId(managerId);
        
        for (SalesPerson subordinate : directSubordinates) {
            subordinateIds.add(subordinate.getId());
            // Recursively get subordinates of this subordinate
            getSubordinateIdsRecursive(subordinate.getId(), subordinateIds);
        }
    }

    private void propagateChangesToOrders(SalesPerson salesPerson) {
        log.info("Propagating salesperson changes to orders for ID: {}", salesPerson.getId());
    }

    private SalesPersonResponse convertToResponse(SalesPerson salesPerson) {
        SalesPersonResponse response = new SalesPersonResponse();
        
        // Basic salesperson info
        response.setId(salesPerson.getId());
        response.setName(salesPerson.getName());
        response.setRole(salesPerson.getRole());
        response.setZone(salesPerson.getZone());
        response.setRegion(salesPerson.getRegion());
        response.setManagerId(salesPerson.getManagerId());
        
        // User account fields
        response.setUsername(salesPerson.getUsername());
        response.setStatus(salesPerson.getStatus());
        response.setFirstName(salesPerson.getFirstName());
        response.setLastName(salesPerson.getLastName());
        response.setPassword(salesPerson.getPassword());

        // Contact information
        response.setContactNo(salesPerson.getContactNo());
        response.setAlternateContactNo(salesPerson.getAlternateContactNo());
        response.setPhone(salesPerson.getPhone()); // Legacy field
        response.setEmail(salesPerson.getEmail());
        
        // Personal details
        response.setBloodGroup(salesPerson.getBloodGroup());
        response.setCompleteAddress(salesPerson.getCompleteAddress());
        response.setDateOfBirth(salesPerson.getDateOfBirth());
        response.setGender(salesPerson.getGender());
        response.setCity(salesPerson.getCity());
        response.setCountry(salesPerson.getCountry());
        response.setZip(salesPerson.getZip());
        
        // Work details
        response.setEmployeeRollNo(salesPerson.getEmployeeRollNo());
        
        // Timestamps
        response.setCreatedAt(salesPerson.getCreatedAt());
        response.setActive(salesPerson.getActive());
        
        return response;
    }
}
