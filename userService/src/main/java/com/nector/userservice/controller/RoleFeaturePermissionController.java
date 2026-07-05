package com.nector.userservice.controller;

import com.nector.userservice.common.features.Features;
import com.nector.userservice.dto.PermissionRequest;
import com.nector.userservice.interceptors.userLogin.model.FeaturePermissionDTO;
import com.nector.userservice.model.RoleFeaturePermission;
import com.nector.userservice.repository.RoleFeaturePermissionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/role-feature-permissions")
@RequiredArgsConstructor
@Slf4j
public class RoleFeaturePermissionController {

    private final RoleFeaturePermissionRepository permissionRepository;

    // ----------------------------------------------------------------
    // GET endpoints
    // ----------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<RoleFeaturePermission>> getAllPermissions() {
        return ResponseEntity.ok(permissionRepository.findAll());
    }

    @GetMapping("/features")
    @Operation(summary = "Get all available features")
    public ResponseEntity<List<Map<String, Object>>> getAllFeatures() {
        List<Map<String, Object>> features = new ArrayList<>();
        for (Features feature : Features.values()) {
            Map<String, Object> featureMap = new HashMap<>();
            featureMap.put("id", com.nector.userservice.common.RoleFeatureMapping.getFeatureId(feature));
            featureMap.put("name", feature.name());
            featureMap.put("displayName", feature.getDisplayName());
            featureMap.put("path", feature.getPath());
            features.add(featureMap);
        }
        return ResponseEntity.ok(features);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get permissions for a user")
    public ResponseEntity<List<FeaturePermissionDTO>> getPermissionsByUser(@PathVariable Long userId) {
        List<FeaturePermissionDTO> permissions = permissionRepository.findByUserId(userId)
                .stream()
                .map(p -> new FeaturePermissionDTO(p.getUserId(), p.getFeatureId(),
                        p.getFeature().name(), p.getCanCreate(), p.getCanRead(), p.getCanUpdate(), p.getCanDelete()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/user/{userId}/feature/{featureId}")
    public ResponseEntity<RoleFeaturePermission> getUserFeaturePermission(
            @PathVariable Long userId, @PathVariable Integer featureId) {
        List<RoleFeaturePermission> results = permissionRepository.findByUserIdAndFeatureId(userId, featureId);
        if (!results.isEmpty()) {
            return ResponseEntity.ok(results.get(0));
        }
        // No record yet — return default all-false so frontend can display current state
        Features feature = findFeatureById(featureId);
        return ResponseEntity.ok(new RoleFeaturePermission(userId, featureId, feature));
    }

    // ----------------------------------------------------------------
    // PUT — single feature
    // ----------------------------------------------------------------

    @Transactional
    @PutMapping(value = "/user/{userId}/feature/{featureId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create or update a single feature permission for a user")
    @ApiResponse(responseCode = "200", description = "Permission updated successfully")
    public ResponseEntity<?> createOrUpdatePermission(
            @PathVariable Long userId,
            @PathVariable Integer featureId,
            @RequestBody PermissionRequest req) {
        try {
            Features feature = findFeatureById(featureId);
            List<RoleFeaturePermission> existing = permissionRepository.findByUserIdAndFeatureId(userId, featureId);

            RoleFeaturePermission permission = existing.isEmpty()
                    ? new RoleFeaturePermission(userId, featureId, feature)
                    : existing.get(0);

            permission.setCanCreate(req.getCanCreate() != null ? req.getCanCreate() : false);
            permission.setCanRead(req.getCanRead() != null ? req.getCanRead() : false);
            permission.setCanUpdate(req.getCanUpdate() != null ? req.getCanUpdate() : false);
            permission.setCanDelete(req.getCanDelete() != null ? req.getCanDelete() : false);

            RoleFeaturePermission saved = permissionRepository.save(permission);
            log.info("Permission updated for userId: {}, featureId: {}", userId, featureId);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Error updating permission for userId: {}, featureId: {}: {}", userId, featureId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update permission: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ----------------------------------------------------------------
    // Legacy aliases — /role/{userId}/... forwards to /user/{userId}/...
    // (frontend still uses the old path)
    // ----------------------------------------------------------------

    @GetMapping("/role/{userId}")
    public ResponseEntity<List<FeaturePermissionDTO>> getPermissionsByUserLegacy(@PathVariable Long userId) {
        return getPermissionsByUser(userId);
    }

    @GetMapping("/role/{userId}/feature/{featureId}")
    public ResponseEntity<RoleFeaturePermission> getUserFeaturePermissionLegacy(
            @PathVariable Long userId, @PathVariable Integer featureId) {
        return getUserFeaturePermission(userId, featureId);
    }

    @PutMapping(value = "/role/{userId}/feature/{featureId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createOrUpdatePermissionLegacy(
            @PathVariable Long userId,
            @PathVariable Integer featureId,
            @RequestBody PermissionRequest req) {
        return createOrUpdatePermission(userId, featureId, req);
    }

    @PutMapping(value = "/role/{userId}/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> bulkUpdatePermissionsLegacy(
            @PathVariable Long userId,
            @RequestBody Map<Integer, PermissionRequest> featurePermissions) {
        return bulkUpdatePermissions(userId, featurePermissions);
    }

    // ----------------------------------------------------------------
    // PUT — bulk features
    // ----------------------------------------------------------------

    @Transactional
    @PutMapping(value = "/user/{userId}/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bulk update feature permissions for a user")
    @ApiResponse(responseCode = "200", description = "Permissions updated successfully")
    public ResponseEntity<?> bulkUpdatePermissions(
            @PathVariable Long userId,
            @RequestBody Map<Integer, PermissionRequest> featurePermissions) {
        try {
            List<RoleFeaturePermission> updated = new ArrayList<>();

            for (Map.Entry<Integer, PermissionRequest> entry : featurePermissions.entrySet()) {
                Integer featureId = entry.getKey();
                PermissionRequest perms = entry.getValue();

                Features feature = findFeatureById(featureId);
                List<RoleFeaturePermission> existing = permissionRepository.findByUserIdAndFeatureId(userId, featureId);

                RoleFeaturePermission permission = existing.isEmpty()
                        ? new RoleFeaturePermission(userId, featureId, feature)
                        : existing.get(0);

                permission.setCanCreate(perms.getCanCreate() != null ? perms.getCanCreate() : false);
                permission.setCanRead(perms.getCanRead() != null ? perms.getCanRead() : false);
                permission.setCanUpdate(perms.getCanUpdate() != null ? perms.getCanUpdate() : false);
                permission.setCanDelete(perms.getCanDelete() != null ? perms.getCanDelete() : false);

                updated.add(permissionRepository.save(permission));
            }

            log.info("Bulk permissions updated for userId: {}, features: {}", userId, updated.size());
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            log.error("Error bulk updating permissions for userId: {}: {}", userId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bulk update failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    @Transactional
    @DeleteMapping("/user/{userId}/feature/{featureId}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long userId, @PathVariable Integer featureId) {
        List<RoleFeaturePermission> existing = permissionRepository.findByUserIdAndFeatureId(userId, featureId);
        if (!existing.isEmpty()) {
            permissionRepository.deleteAll(existing);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ----------------------------------------------------------------
    // Permission matrix (grouped by userId)
    // ----------------------------------------------------------------

    @GetMapping("/matrix")
    public ResponseEntity<Map<Long, Map<Integer, RoleFeaturePermission>>> getPermissionMatrix() {
        Map<Long, Map<Integer, RoleFeaturePermission>> matrix = permissionRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        RoleFeaturePermission::getUserId,
                        Collectors.toMap(RoleFeaturePermission::getFeatureId, p -> p)
                ));
        return ResponseEntity.ok(matrix);
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------

    private Features findFeatureById(Integer featureId) {
        return switch (featureId) {
            case 1  -> Features.DASHBOARD;
            case 2  -> Features.ACCOUNTS;
            case 3  -> Features.HR;
            case 4  -> Features.DISTRIBUTOR;
            case 5  -> Features.INVENTORY;
            case 6  -> Features.SALES;
            case 7  -> Features.REPORTS;
            case 8  -> Features.COMPLAINT;
            case 9  -> Features.PRODUCTS;
            case 10 -> Features.ORDER_DETAILS;
            case 11 -> Features.LOGISTIC;
            case 12 -> Features.USER_RIGHTS;
            case 13 -> Features.DISPATCH;
            case 14 -> Features.PRODUCTS_FINISHED_PRODUCTS;
            case 15 -> Features.PRODUCTS_RAW_MATERIALS;
            case 16 -> Features.PRODUCTS_MACHINE_PARTS;
            case 17 -> Features.INVENTORY_MASTERS;
            case 18 -> Features.INVENTORY_TRANSACTIONS;
            case 19 -> Features.INVENTORY_TRANSACTIONS_PROFORMA_INVOICE;
            case 20 -> Features.INVENTORY_TRANSACTIONS_PO_DEPOSIT_RECEIPTS_LIST;
            case 21 -> Features.INVENTORY_TRANSACTIONS_PO_LIST;
            case 22 -> Features.INVENTORY_TRANSACTIONS_OUTWARD_CHALLAN;
            case 23 -> Features.INVENTORY_TRANSACTIONS_SALE_INVOICE;
            case 24 -> Features.INVENTORY_INWARD;
            case 25 -> Features.INVENTORY_OUTWARD;
            case 26 -> Features.TRANSACTION_CASHBOOK;
            case 27 -> Features.TRANSACTION_MASTER;
            case 28 -> Features.ADMIN_APPROVAL;
            case 29 -> Features.ACCOUNTS_MASTER;
            case 30 -> Features.ACCOUNTS_PAYMENT_REQUESTS;
            case 31 -> Features.ACCOUNTS_PI_UPDATE;
            case 32 -> Features.HR_KRA_KPI;
            case 33 -> Features.COMPLAINTS_MANAGEMENT;
            default -> throw new IllegalArgumentException("Unknown feature ID: " + featureId);
        };
    }
}
