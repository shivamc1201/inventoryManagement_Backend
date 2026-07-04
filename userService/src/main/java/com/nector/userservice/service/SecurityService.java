package com.nector.userservice.service;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.RoleFeatureMapping;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.model.User;
import com.nector.userservice.model.RoleFeaturePermission;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.repository.RoleFeaturePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final UserRepository userRepository;
    private final RoleFeaturePermissionRepository roleFeaturePermissionRepository;

    @Transactional(readOnly = true)
    public boolean currentUserCanCreate(String resourceType) {
        return hasPermission(getCurrentUserId(), "CREATE", resourceType);
    }

    @Transactional(readOnly = true)
    public boolean currentUserCanRead(String resourceType) {
        return hasPermission(getCurrentUserId(), "READ", resourceType);
    }

    @Transactional(readOnly = true)
    public boolean currentUserCanUpdate(String resourceType) {
        return hasPermission(getCurrentUserId(), "UPDATE", resourceType);
    }

    @Transactional(readOnly = true)
    public boolean currentUserCanDelete(String resourceType) {
        return hasPermission(getCurrentUserId(), "DELETE", resourceType);
    }

    // TODO: Replace with actual Spring Security context implementation
    private Long getCurrentUserId() {
        return 1L;
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String operation, String resourceType) {
        log.debug("Checking permission - userId: {}, operation: {}, resourceType: {}", userId, operation, resourceType);
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User not found: {}", userId);
                return false;
            }

            Features feature;
            try {
                feature = Features.valueOf(resourceType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown feature: {}", resourceType);
                return false;
            }

            Integer featureId = RoleFeatureMapping.getFeatureId(feature);
            List<RoleFeaturePermission> results = roleFeaturePermissionRepository.findByUserIdAndFeatureId(userId, featureId);

            if (!results.isEmpty()) {
                boolean canPerform = results.get(0).hasPermission(operation);
                log.debug("Permission check: {} for userId: {}, operation: {}, feature: {}", canPerform, userId, operation, resourceType);
                return canPerform;
            }

            // Fallback to role-based defaults when no explicit permission is set
            boolean canPerform = switch (user.getRoleType()) {
                case SUPER_ADMIN, ADMIN -> true;
                case BUSINESS_DEV_MGR, PLANT_MGR, HR_MGR, LOGISTICS_MGR,
                     ACCOUNT_MGR, NATIONAL_SALES_MGR, STATE_SALES_MGR,
                     ZONAL_SALES_MGR, REGIONAL_SALES_MGR, AREA_SALES_MGR ->
                        !"DELETE".equals(operation);
                case ACCOUNT_OFFICER, ACCOUNT_EXECUTIVE, SALES_OFFICER,
                     SALES_EXECUTIVE, LOGISTICS_OFFICER, DISPATCH, HR_EXECUTIVE,
                     PLANT_OFFICER, PLANT_EXECUTIVE ->
                        !"CREATE".equals(operation) && !"DELETE".equals(operation);
                case Distributor -> "READ".equals(operation);
                default -> false;
            };

            log.debug("Fallback permission: {} for userId: {}, operation: {}, feature: {}", canPerform, userId, operation, resourceType);
            return canPerform;

        } catch (Exception e) {
            log.error("Error checking permission for userId: {}", userId, e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<Features> getCurrentUserFeatures(String operation) {
        Long userId = getCurrentUserId();
        return roleFeaturePermissionRepository.findByUserId(userId)
                .stream()
                .filter(p -> p.hasPermission(operation))
                .map(RoleFeaturePermission::getFeature)
                .toList();
    }
}
