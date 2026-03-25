package com.nector.userservice.service;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Security service for Spring Security RBAC integration
 * Provides CRUD permission checking based on user roles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public boolean currentUserCanCreate(String resourceType) {
        log.debug("Checking create permission for resourceType: {}", resourceType);
        return hasPermission(1L, "CREATE", resourceType); // Using test user ID
    }
    
    @Transactional(readOnly = true)
    public boolean currentUserCanRead(String resourceType) {
        log.debug("Checking read permission for resourceType: {}", resourceType);
        return hasPermission(1L, "READ", resourceType); // Using test user ID
    }
    
    @Transactional(readOnly = true)
    public boolean currentUserCanUpdate(String resourceType) {
        log.debug("Checking update permission for resourceType: {}", resourceType);
        return hasPermission(1L, "UPDATE", resourceType); // Using test user ID
    }
    
    @Transactional(readOnly = true)
    public boolean currentUserCanDelete(String resourceType) {
        log.debug("Checking delete permission for resourceType: {}", resourceType);
        return hasPermission(1L, "DELETE", resourceType); // Using test user ID
    }
    
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String operation, String resourceType) {
        log.debug("Checking permission - userId: {}, operation: {}, resourceType: {}", userId, operation, resourceType);
        
        try {
            User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElse(null);
            
            if (user == null) {
                log.warn("User not found: {}", userId);
                return false;
            }
            
            // Determine permission level based on roleType
            boolean canPerform = switch (user.getRoleType()) {
                case SUPER_ADMIN, ADMIN -> {
                    // Admin can do everything
                    yield true;
                }
                case BUSINESS_DEV_MGR, PLANT_MGR, HR_MGR, LOGISTICS_MGR, 
                     ACCOUNT_MGR, NATIONAL_SALES_MGR, STATE_SALES_MGR, 
                     ZONAL_SALES_MGR, REGIONAL_SALES_MGR, AREA_SALES_MGR -> {
                    // Managers can create, read, update but not delete
                    yield !"DELETE".equals(operation);
                }
                case ACCOUNT_OFFICER, ACCOUNT_EXECUTIVE, SALES_OFFICER, 
                     SALES_EXECUTIVE, LOGISTICS_OFFICER, DISPATCH, HR_EXECUTIVE, 
                     PLANT_OFFICER, PLANT_EXECUTIVE -> {
                    // Officers can only read and update
                    yield !"CREATE".equals(operation) && !"DELETE".equals(operation);
                }
                case Distributor -> {
                    // Distributors can only read
                    yield "READ".equals(operation);
                }
                default -> false;
            };
            
            log.debug("Permission check result: {} for userId: {}, operation: {}, resourceType: {}", 
                canPerform, userId, operation, resourceType);
            return canPerform;
            
        } catch (Exception e) {
            log.error("Error checking permission for userId: {}", userId, e);
            return false;
        }
    }
}
