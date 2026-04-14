package com.nector.userservice.service;

import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.repository.SalesPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to handle authentication context and get current user information
 * TODO: Integrate with actual Spring Security context
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationContextService {

    private final SalesPersonRepository salesPersonRepository;

    /**
     * Get current salesperson ID from authentication context
     * For now, this is a placeholder that should be replaced with actual Spring Security integration
     * 
     * @return Current salesperson ID or null if not found
     */
    @Transactional(readOnly = true)
    public Long getCurrentSalespersonId() {
        // TODO: Replace with actual Spring Security context
        // This should extract the salesperson ID from JWT token or security context
        
        // For demonstration purposes, you can pass the ID as a request parameter
        // In production, this would be extracted from the authentication token
        
        log.debug("Getting current salesperson ID from authentication context");
        
        // Placeholder - in real implementation, this would be:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // String username = auth.getName();
        // SalesPerson salesPerson = salesPersonRepository.findByUsername(username);
        // return salesPerson != null ? salesPerson.getId() : null;
        
        return null; // Let the controller handle this via request parameter for now
    }

    /**
     * Get current salesperson entity
     */
    @Transactional(readOnly = true)
    public SalesPerson getCurrentSalesperson() {
        Long salespersonId = getCurrentSalespersonId();
        if (salespersonId == null) {
            return null;
        }
        
        return salesPersonRepository.findById(salespersonId).orElse(null);
    }

    /**
     * Check if current user is a National Sales Manager
     */
    @Transactional(readOnly = true)
    public boolean isCurrentUserNSM() {
        SalesPerson current = getCurrentSalesperson();
        return current != null && current.getRole() == com.nector.userservice.enums.SalesRole.NATIONAL_SALES_MGR;
    }

    /**
     * Check if current user can view all salespersons
     */
    @Transactional(readOnly = true)
    public boolean canCurrentUserViewAll() {
        SalesPerson current = getCurrentSalesperson();
        return current != null && current.getRole() == com.nector.userservice.enums.SalesRole.NATIONAL_SALES_MGR;
    }

    /**
     * Validate that the current user has permission to view the requested salesperson's hierarchy
     */
    @Transactional(readOnly = true)
    public boolean canViewHierarchyOf(Long targetSalespersonId) {
        // If user is NSM, they can view any hierarchy
        if (isCurrentUserNSM()) {
            return true;
        }

        Long currentId = getCurrentSalespersonId();
        if (currentId == null) {
            return false;
        }

        // User can always view their own hierarchy
        if (currentId.equals(targetSalespersonId)) {
            return true;
        }

        // For other roles, implement additional business logic if needed
        // For now, only NSM can view other people's hierarchy
        return false;
    }
}
