package com.nector.userservice.service;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.features.Features;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * Service to initialize sample role-permission mappings
 * This runs after EnumSyncService to set up default permissions for roles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {
    
    private final RoleManagementService roleManagementService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Entering run() - Starting data initialization");
        initializeDefaultPermissions();
        log.info("Exiting run() - Data initialization completed");
    }
    
    private void initializeDefaultPermissions() {
        log.info("Entering initializeDefaultPermissions()");
        
        try {
            assignAllPermissionsToAdmin();
            
            assignPermissionsToRole(RoleType.BUSINESS_DEV_MGR, 
                Features.DASHBOARD, Features.DISTRIBUTOR, Features.REPORTS);
            
            assignPermissionsToRole(RoleType.ACCOUNT_MGR, 
                Features.DASHBOARD, Features.ACCOUNTS, Features.REPORTS);
            assignPermissionsToRole(RoleType.ACCOUNT_OFFICER, 
                Features.DASHBOARD, Features.ACCOUNTS);
            assignPermissionsToRole(RoleType.ACCOUNT_EXECUTIVE, 
                Features.DASHBOARD, Features.ACCOUNTS);
            
            assignPermissionsToRole(RoleType.NATIONAL_SALES_MGR, 
                Features.DASHBOARD, Features.DISTRIBUTOR, Features.REPORTS);
            assignPermissionsToRole(RoleType.STATE_SALES_MGR, 
                Features.DASHBOARD, Features.DISTRIBUTOR, Features.REPORTS);
            assignPermissionsToRole(RoleType.ZONAL_SALES_MGR, 
                Features.DASHBOARD, Features.DISTRIBUTOR, Features.REPORTS);
            assignPermissionsToRole(RoleType.REGIONAL_SALES_MGR, 
                Features.DASHBOARD, Features.DISTRIBUTOR);
            assignPermissionsToRole(RoleType.AREA_SALES_MGR, 
                Features.DASHBOARD, Features.DISTRIBUTOR);
            assignPermissionsToRole(RoleType.SALES_OFFICER, 
                Features.DASHBOARD, Features.DISTRIBUTOR);
            assignPermissionsToRole(RoleType.SALES_EXECUTIVE, 
                Features.DASHBOARD, Features.DISTRIBUTOR);
            
            assignPermissionsToRole(RoleType.HR_MGR, 
                Features.DASHBOARD, Features.HR, Features.USER_RIGHTS, Features.REPORTS);
            assignPermissionsToRole(RoleType.HR_EXECUTIVE, 
                Features.DASHBOARD, Features.HR);
            
            assignPermissionsToRole(RoleType.LOGISTICS_MGR, 
                Features.DASHBOARD, Features.INVENTORY, Features.REPORTS);
            assignPermissionsToRole(RoleType.LOGISTICS_OFFICER, 
                Features.DASHBOARD, Features.INVENTORY);
            
            assignPermissionsToRole(RoleType.PLANT_MGR, 
                Features.DASHBOARD, Features.INVENTORY, Features.INVENTORY_MASTERS, 
                Features.INVENTORY_TRANSACTIONS, Features.REPORTS);
            assignPermissionsToRole(RoleType.PLANT_OFFICER, 
                Features.DASHBOARD, Features.INVENTORY, Features.INVENTORY_MASTERS);
            assignPermissionsToRole(RoleType.PLANT_EXECUTIVE, 
                Features.DASHBOARD, Features.INVENTORY);
            
            log.info("Default role-permission mappings initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing default permissions: {}", e.getMessage());
        }
        
        log.info("Exiting initializeDefaultPermissions()");
    }
    
    private void assignAllPermissionsToAdmin() {
        log.debug("Entering assignAllPermissionsToAdmin()");
        
        for (Features feature : Features.values()) {
            try {
                roleManagementService.assignPermissionToRole(RoleType.ADMIN, feature);
            } catch (Exception e) {
                // Permission might already be assigned, ignore
            }
        }
        
        log.debug("Exiting assignAllPermissionsToAdmin()");
    }
    
    private void assignPermissionsToRole(RoleType roleType, Features... features) {
        log.debug("Entering assignPermissionsToRole() for role: {} with {} features", roleType, features.length);
        
        for (Features feature : features) {
            try {
                roleManagementService.assignPermissionToRole(roleType, feature);
            } catch (Exception e) {
                // Permission might already be assigned, ignore
            }
        }
        
        log.debug("Exiting assignPermissionsToRole() for role: {}", roleType);
    }
}