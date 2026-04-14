package com.nector.userservice.service;

import com.nector.userservice.dto.SalesPersonResponse;
import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.repository.SalesPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for filtering sales hierarchy based on user's role and position
 * Provides dynamic access control where users can only see their subordinates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesHierarchyFilterService {

    private final SalesPersonRepository salesPersonRepository;
    private final SecurityService securityService;

    /**
     * Get sales hierarchy based on current user's role and position
     * - NSM can see everyone
     * - RSM can see themselves and their subordinates (ASM, Sales Executives)
     * - ASM can see themselves and their subordinates (Sales Executives)
     * - Sales Executive can only see themselves
     */
    @Transactional(readOnly = true)
    public List<SalesPersonResponse> getFilteredSalesHierarchy(Long currentSalespersonId) {
        log.info("Getting filtered sales hierarchy for salesperson ID: {}", currentSalespersonId);
        
        if (currentSalespersonId == null) {
            log.warn("No salesperson ID provided, returning empty list");
            return new ArrayList<>();
        }

        // Get current salesperson
        SalesPerson currentUser = salesPersonRepository.findById(currentSalespersonId)
                .orElseThrow(() -> new RuntimeException("Salesperson not found"));

        if (!currentUser.getActive()) {
            log.warn("Current salesperson is not active: {}", currentSalespersonId);
            return new ArrayList<>();
        }

        List<SalesPerson> filteredSalespersons = new ArrayList<>();
        
        switch (currentUser.getRole()) {
            case NATIONAL_SALES_MGR:
                // NSM can see everyone
                filteredSalespersons = salesPersonRepository.findByActiveTrue();
                log.info("NSM user {} can see all {} salespersons", 
                        currentSalespersonId, filteredSalespersons.size());
                break;
                
            case REGIONAL_SALES_MGR:
                // RSM can see themselves and all their subordinates recursively
                filteredSalespersons = getSalespersonAndSubordinates(currentSalespersonId);
                log.info("RSM user {} can see {} salespersons (self + subordinates)", 
                        currentSalespersonId, filteredSalespersons.size());
                break;
                
            case AREA_SALES_MGR:
                // ASM can see themselves and all their subordinates recursively
                filteredSalespersons = getSalespersonAndSubordinates(currentSalespersonId);
                log.info("ASM user {} can see {} salespersons (self + subordinates)", 
                        currentSalespersonId, filteredSalespersons.size());
                break;
                
            case SALES_OFFICER:
            case SALES_EXECUTIVE:
                // Sales Executive/Officer can only see themselves
                filteredSalespersons = List.of(currentUser);
                log.info("Sales Executive/Officer user {} can see only themselves", 
                        currentSalespersonId);
                break;
                
            default:
                log.warn("Unknown role {} for salesperson {}", currentUser.getRole(), currentSalespersonId);
                return new ArrayList<>();
        }

        return filteredSalespersons.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get salesperson and all their subordinates recursively
     */
    private List<SalesPerson> getSalespersonAndSubordinates(Long salespersonId) {
        List<SalesPerson> result = new ArrayList<>();
        
        // Add the salesperson themselves
        SalesPerson current = salesPersonRepository.findById(salespersonId)
                .orElseThrow(() -> new RuntimeException("Salesperson not found"));
        result.add(current);
        
        // Add all subordinates recursively
        addSubordinatesRecursive(salespersonId, result);
        
        return result;
    }

    /**
     * Recursively add all subordinates of a salesperson
     */
    private void addSubordinatesRecursive(Long managerId, List<SalesPerson> result) {
        List<SalesPerson> directSubordinates = salesPersonRepository.findActiveByManagerId(managerId);
        
        for (SalesPerson subordinate : directSubordinates) {
            result.add(subordinate);
            // Recursively add subordinates of this subordinate
            addSubordinatesRecursive(subordinate.getId(), result);
        }
    }

    /**
     * Get sales hierarchy by specific salesperson ID (for admin/NSM use)
     * This allows viewing hierarchy from any salesperson's perspective
     */
    @Transactional(readOnly = true)
    public List<SalesPersonResponse> getSalesHierarchyByPerspective(Long perspectiveSalespersonId) {
        log.info("Getting sales hierarchy from perspective of salesperson ID: {}", perspectiveSalespersonId);
        
        // Validate that the perspective salesperson exists and is active
        SalesPerson perspectiveUser = salesPersonRepository.findById(perspectiveSalespersonId)
                .orElseThrow(() -> new RuntimeException("Salesperson not found"));

        if (!perspectiveUser.getActive()) {
            log.warn("Perspective salesperson is not active: {}", perspectiveSalespersonId);
            return new ArrayList<>();
        }

        return getFilteredSalesHierarchy(perspectiveSalespersonId);
    }

    /**
     * Get all salespersons that the current user can manage/edit
     * This is for admin operations like creating/updating subordinates
     */
    @Transactional(readOnly = true)
    public List<SalesPersonResponse> getManageableSalespersons(Long currentSalespersonId) {
        log.info("Getting manageable salespersons for salesperson ID: {}", currentSalespersonId);
        
        if (currentSalespersonId == null) {
            return new ArrayList<>();
        }

        // Get current salesperson
        SalesPerson currentUser = salesPersonRepository.findById(currentSalespersonId)
                .orElseThrow(() -> new RuntimeException("Salesperson not found"));

        if (!currentUser.getActive()) {
            return new ArrayList<>();
        }

        List<SalesPerson> manageableSalespersons = new ArrayList<>();
        
        switch (currentUser.getRole()) {
            case NATIONAL_SALES_MGR:
                // NSM can manage everyone except other NSMs
                manageableSalespersons = salesPersonRepository.findByActiveTrue().stream()
                        .filter(sp -> sp.getRole() != SalesRole.NATIONAL_SALES_MGR)
                        .collect(Collectors.toList());
                break;
                
            case REGIONAL_SALES_MGR:
                // RSM can manage ASMs and Sales Executives in their hierarchy
                manageableSalespersons = getSubordinatesByRole(currentSalespersonId, 
                        List.of(SalesRole.AREA_SALES_MGR, SalesRole.SALES_OFFICER, SalesRole.SALES_EXECUTIVE));
                break;
                
            case AREA_SALES_MGR:
                // ASM can manage Sales Executives in their hierarchy
                manageableSalespersons = getSubordinatesByRole(currentSalespersonId, 
                        List.of(SalesRole.SALES_OFFICER, SalesRole.SALES_EXECUTIVE));
                break;
                
            case SALES_OFFICER:
            case SALES_EXECUTIVE:
                // Sales Executive cannot manage anyone
                manageableSalespersons = new ArrayList<>();
                break;
                
            default:
                manageableSalespersons = new ArrayList<>();
        }

        return manageableSalespersons.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get subordinates of specific roles recursively
     */
    private List<SalesPerson> getSubordinatesByRole(Long managerId, List<SalesRole> targetRoles) {
        List<SalesPerson> result = new ArrayList<>();
        getSubordinatesByRoleRecursive(managerId, targetRoles, result);
        return result;
    }

    private void getSubordinatesByRoleRecursive(Long managerId, List<SalesRole> targetRoles, List<SalesPerson> result) {
        List<SalesPerson> directSubordinates = salesPersonRepository.findActiveByManagerId(managerId);
        
        for (SalesPerson subordinate : directSubordinates) {
            if (targetRoles.contains(subordinate.getRole())) {
                result.add(subordinate);
            }
            // Continue recursion for all subordinates
            getSubordinatesByRoleRecursive(subordinate.getId(), targetRoles, result);
        }
    }

    /**
     * Convert SalesPerson entity to SalesPersonResponse DTO
     */
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
        
        // Contact information
        response.setContactNo(salesPerson.getContactNo());
        response.setAlternateContactNo(salesPerson.getAlternateContactNo());
        response.setPhone(salesPerson.getPhone());
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
