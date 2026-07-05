package com.nector.userservice.interceptors.userLogin.impl;

import com.nector.userservice.common.BaseLoginResponse;
import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.dto.UserDetailsDTO;
import com.nector.userservice.interceptors.distributor.model.Distributor;
import com.nector.userservice.interceptors.distributor.model.DistributorLoginResponse;
import com.nector.userservice.interceptors.distributor.model.DistributorStatus;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;
import com.nector.userservice.interceptors.userLogin.model.UnifiedLoginResponse;
import com.nector.userservice.interceptors.userLogin.model.FeaturePermissionDTO;
import com.nector.userservice.interceptors.userLogin.service.LoginService;
import com.nector.userservice.model.RoleFeaturePermission;
import com.nector.userservice.repository.RoleFeaturePermissionRepository;
import com.nector.userservice.model.Role;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.repository.SalesPersonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.nector.userservice.common.RoleFeatureMapping;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;
    private final DistributorRepository distributorRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final RoleFeaturePermissionRepository roleFeaturePermissionRepository;

    @Override
    public BaseLoginResponse authenticate(LoginRequest request) {
        log.info("Login attempt for {}", request.getUsername());

        Optional<Distributor> distOpt = distributorRepository.findByUsername(request.getUsername());
        if (distOpt.isPresent()) {
            return authenticateDistributor(distOpt.get(), request);
        }

        Optional<SalesPerson> salesPersonOpt = salesPersonRepository.findByUsername(request.getUsername());
        if (salesPersonOpt.isPresent()) {
            return authenticateSalesPersonFromTable(salesPersonOpt.get(), request);
        }

        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isPresent()) {
            User user = userRepository.findByUsernameWithRolesAndPermissions(request.getUsername())
                    .orElse(userOpt.get());
            return authenticateNormalUser(user, request);
        }

        log.warn("Username not found {}", request.getUsername());
        throw new RuntimeException("Username not available");
    }

    private LoginResponse authenticateSalesPersonFromTable(SalesPerson salesPerson, LoginRequest request) {
        if (salesPerson.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Salesperson account inactive");
        }
        if (!salesPerson.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        RoleType roleType = convertSalesRoleToRoleType(salesPerson.getRole());
        List<Object> featureDetails = getSalesPersonFeatures(roleType);
        Set<String> featureNames = getSalesPersonFeatureNames(roleType);

        log.info("Salesperson logged in: {}", request.getUsername());
        LoginResponse response = new LoginResponse(
                null, "Bearer", request.getUsername(),
                "Login successful for Salesperson " + request.getUsername(),
                roleType.name(), salesPerson.getId(), featureDetails, featureNames, "LOGGED_IN");
        response.setName(salesPerson.getFirstName() + " " + salesPerson.getLastName());
        return response;
    }

    private RoleType convertSalesRoleToRoleType(SalesRole salesRole) {
        return switch (salesRole) {
            case NATIONAL_SALES_MGR  -> RoleType.NATIONAL_SALES_MGR;
            case STATE_SALES_MGR     -> RoleType.STATE_SALES_MGR;
            case ZONAL_SALES_MGR     -> RoleType.ZONAL_SALES_MGR;
            case REGIONAL_SALES_MGR  -> RoleType.REGIONAL_SALES_MGR;
            case AREA_SALES_MGR      -> RoleType.AREA_SALES_MGR;
            case SALES_OFFICER       -> RoleType.SALES_OFFICER;
            default                  -> RoleType.SALES_EXECUTIVE;
        };
    }

    private LoginResponse authenticateNormalUser(User user, LoginRequest request) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Username inactive, Contact ADMIN");
        }
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        user.setLoggedIn(true);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        log.info("[DEBUG] User: {}, userId: {}, roleType: {}", user.getUsername(), user.getId(), user.getRoleType());
        log.info("[DEBUG] user.getRoles() size: {}", user.getRoles().size());
        user.getRoles().forEach(r -> log.info("[DEBUG] Role entity: id={}, roleType={}, name={}", r.getId(), r.getRoleType(), r.getName()));

        Set<Features> features;
        boolean isAdmin = user.getRoleType() == RoleType.ADMIN ||
                          user.getRoleType() == RoleType.SUPER_ADMIN ||
                          user.getRoleType() == RoleType.DISPATCH ||
                          user.getRoles().stream()
                              .map(Role::getRoleType)
                              .anyMatch(rt -> rt == RoleType.ADMIN || rt == RoleType.SUPER_ADMIN || rt == RoleType.DISPATCH);

        log.info("[DEBUG] isAdmin: {}", isAdmin);

        if (isAdmin) {
            features = Arrays.stream(Features.values()).collect(Collectors.toSet());
            log.info("[DEBUG] Admin user - all features: {}", features.size());
        } else {
            features = new HashSet<>();
            Long userId = user.getId();
            log.info("[DEBUG] Querying role_feature_permissions by userId: {}", userId);
            List<RoleFeaturePermission> permissions = roleFeaturePermissionRepository.findByUserId(userId);
            log.info("[DEBUG] Found {} permissions for userId: {}", permissions.size(), userId);

            permissions.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getCanCreate()) ||
                                 Boolean.TRUE.equals(p.getCanRead()) ||
                                 Boolean.TRUE.equals(p.getCanUpdate()))
                    .map(RoleFeaturePermission::getFeature)
                    .filter(Objects::nonNull)
                    .forEach(features::add);

            log.info("[DEBUG] Total features with at least one permission: {}", features.size());
        }

        List<Object> featureDetails = features.stream()
                .map(f -> Map.of("name", f.name(), "displayName", f.getDisplayName(), "path", f.getPath()))
                .collect(Collectors.toList());
        Set<String> featureNames = features.stream().map(Features::name).collect(Collectors.toSet());

        return new LoginResponse(
                null, "Bearer", request.getUsername(),
                "Login successful for User " + request.getUsername(),
                user.getRoleType().name(), user.getId(), featureDetails, featureNames, "LOGGED_IN");
    }

    private DistributorLoginResponse authenticateDistributor(Distributor user, LoginRequest request) {
        if (user.getStatus() != DistributorStatus.ACTIVE) {
            throw new RuntimeException("Username inactive, Contact ADMIN");
        }
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        List<Object> featureDetails = List.of(
                Map.of("displayName", "OrderDetails",  "name", "ORDER_DETAILS",  "path", "/order-details"),
                Map.of("displayName", "Dashboard",     "name", "DASHBOARD",      "path", "/dashboard"),
                Map.of("displayName", "Reports",       "name", "REPORTS",        "path", "/reports"),
                Map.of("displayName", "Products",      "name", "PRODUCTS",       "path", "/products"),
                Map.of("displayName", "Complaint",     "name", "COMPLAINT",      "path", "/complaint"),
                Map.of("displayName", "PlaceOrder",    "name", "PLACE_ORDER",    "path", "/placeOrder")
        );
        Set<String> featureNames = Set.of("ORDER_DETAILS", "PRODUCTS", "DASHBOARD", "REPORTS", "COMPLAINT", "PLACE_ORDER");

        log.info("Distributor logged in: {}", request.getUsername());
        return new DistributorLoginResponse(
                null, "Bearer", request.getUsername(),
                "Login successful for Distributor " + request.getUsername(),
                user.getId(), "distributor", featureDetails, featureNames,
                user.getFirstName(), user.getDistributorCode(), "LOGGED_IN");
    }

    private List<Object> getSalesPersonFeatures(RoleType roleType) {
        return List.of(
                Map.of("displayName", "Dashboard",    "name", "DASHBOARD",    "path", "/dashboard"),
                Map.of("displayName", "OrderDetails", "name", "ORDER_DETAILS","path", "/order-details"),
                Map.of("displayName", "Products",     "name", "PRODUCTS",     "path", "/products"),
                Map.of("displayName", "Reports",      "name", "REPORTS",      "path", "/reports"),
                Map.of("displayName", "Analytics",    "name", "ANALYTICS",    "path", "/analytics"),
                Map.of("displayName", "Sales",        "name", "SALES",        "path", "/sales")
        );
    }

    private Set<String> getSalesPersonFeatureNames(RoleType roleType) {
        return Set.of("DASHBOARD", "ORDER_DETAILS", "SALES", "REPORTS", "ANALYTICS", "COMPLAINT");
    }

    @Override
    public BaseLoginResponse authenticateWithPermissions(LoginRequest request) {
        log.info("Unified login attempt for {}", request.getUsername());

        Optional<Distributor> distOpt = distributorRepository.findByUsername(request.getUsername());
        if (distOpt.isPresent()) {
            return authenticateDistributor(distOpt.get(), request);
        }

        Optional<SalesPerson> salesPersonOpt = salesPersonRepository.findByUsername(request.getUsername());
        if (salesPersonOpt.isPresent()) {
            return authenticateSalesPersonFromTable(salesPersonOpt.get(), request);
        }

        BaseLoginResponse baseResponse = authenticate(request);
        if (!(baseResponse instanceof LoginResponse loginResponse)) {
            throw new RuntimeException("Unified login only supports regular users");
        }

        User user = userRepository.findByUsernameWithRolesAndPermissions(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FeaturePermissionDTO> rolePermissions = getUserPermissions(user);

        log.info("User logged in on web: {}", request.getUsername());
        return new UnifiedLoginResponse(
                loginResponse.getToken(), loginResponse.getType(), loginResponse.getUsername(),
                loginResponse.getMessage(), loginResponse.getRoleType(), loginResponse.getUserId(),
                loginResponse.getFeatures(), loginResponse.getFeatureNames(),
                loginResponse.getLoginStatus(), rolePermissions);
    }

    private List<FeaturePermissionDTO> getUserPermissions(User user) {
        Long userId = user.getId();
        boolean isAdmin = user.getRoleType() == RoleType.ADMIN || user.getRoleType() == RoleType.SUPER_ADMIN;

        if (isAdmin) {
            return Arrays.stream(Features.values())
                    .map(f -> new FeaturePermissionDTO(userId, RoleFeatureMapping.getFeatureId(f), f.name(),
                            true, true, true, true))
                    .collect(Collectors.toList());
        }

        log.info("[DEBUG] getUserPermissions - querying by userId: {}", userId);
        return roleFeaturePermissionRepository.findByUserId(userId)
                .stream()
                .filter(perm -> Boolean.TRUE.equals(perm.getCanCreate()) ||
                               Boolean.TRUE.equals(perm.getCanRead()) ||
                               Boolean.TRUE.equals(perm.getCanUpdate()))
                .map(perm -> new FeaturePermissionDTO(
                        perm.getUserId(), perm.getFeatureId(), perm.getFeature().name(),
                        perm.getCanCreate(), perm.getCanRead(), perm.getCanUpdate(), perm.getCanDelete()))
                .collect(Collectors.toList());
    }

    @Override
    public LoginResponse authenticateSecondUser(LoginRequest request) {
        log.info("Entering authenticateSecondUser() for username: {}", request.getUsername());
        return null;
    }

    @Override
    public UserDetailsDTO getUserDetailsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        UserDetailsDTO dto = new UserDetailsDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCreatedOn(user.getCreatedOn());
        dto.setContactNo(user.getContactNo());
        dto.setAlternateContactNo(user.getAlternateContactNo());
        dto.setBloodGroup(user.getBloodGroup());
        dto.setCompleteAddress(user.getCompleteAddress());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setCity(user.getCity());
        dto.setCountry(user.getCountry());
        dto.setZip(user.getZip());
        dto.setLastLoginTime(user.getLastLoginTime());
        dto.setLoggedIn(user.isLoggedIn());
        dto.setPasswordSetDate(user.getPasswordSetDate());
        dto.setRoleType(user.getRoleType());
        return dto;
    }
}
