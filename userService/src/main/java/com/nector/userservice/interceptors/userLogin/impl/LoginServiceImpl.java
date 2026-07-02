package com.nector.userservice.interceptors.userLogin.impl;

import com.nector.userservice.common.BaseLoginResponse;
import com.nector.userservice.common.RoleFeatureMapping;
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
import com.nector.userservice.repository.RoleRepository;
import com.nector.userservice.model.Role;
import com.nector.userservice.model.User;
// import com.nector.userservice.model.UserSession;
import com.nector.userservice.repository.UserRepository;
// import com.nector.userservice.repository.UserSessionRepository;
// import com.nector.userservice.service.JwtService; // JWT disabled
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.repository.SalesPersonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService {
    
    private final UserRepository userRepository;
    // private final UserSessionRepository userSessionRepository; // For future session management
    // private final JwtService jwtService; // JWT disabled
    private final DistributorRepository distributorRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final RoleFeaturePermissionRepository roleFeaturePermissionRepository;
    private final RoleRepository roleRepository;
    

    @Override
    public BaseLoginResponse authenticate(LoginRequest request) {

        log.info("Login attempt for {}", request.getUsername());

        // =========================================================
        // 1️⃣ DISTRIBUTOR LOGIN FIRST
        // =========================================================
        Optional<Distributor> distOpt =
                distributorRepository.findByUsername(request.getUsername());

        if (distOpt.isPresent()) {
            log.info("Distributor login detected for {}", request.getUsername());
            return authenticateDistributor(distOpt.get(), request);
        }

        // =========================================================
        // 2️⃣ SALESPERSON LOGIN CHECK
        // =========================================================
        Optional<SalesPerson> salesPersonOpt =
                salesPersonRepository.findByUsername(request.getUsername());

        if (salesPersonOpt.isPresent()) {
            log.info("Salesperson login detected for {}", request.getUsername());
            return authenticateSalesPersonFromTable(salesPersonOpt.get(), request);
        }

        // =========================================================
        // 3️⃣ USER LOGIN (NORMAL)
        // =========================================================
        Optional<User> userOpt =
                userRepository.findByUsername(request.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Load roles
            user = userRepository
                    .findByUsernameWithRolesAndPermissions(request.getUsername())
                    .orElse(user);

            return authenticateNormalUser(user, request);
        }

        // =========================================================
        // 4️⃣ USER NOT FOUND
        // =========================================================
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

        // String token = jwtService.generateToken(request.getUsername()); // JWT disabled
        String token = null;

        // Update last login time if you have that field
        // salesPerson.setLastLoginTime(LocalDateTime.now());
        // salesPersonRepository.save(salesPerson);

        // Convert SalesRole to RoleType for compatibility
        RoleType roleType = convertSalesRoleToRoleType(salesPerson.getRole());

        List<Object> featureDetails = getSalesPersonFeatures(roleType);
        Set<String> featureNames = getSalesPersonFeatureNames(roleType);

        log.info(" SALESPERSON LOGGED IN .......", request.getUsername());

        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                request.getUsername(),
                "Login successful for Salesperson " + request.getUsername(),
                roleType.name(),
                salesPerson.getId(),
                featureDetails,
                featureNames,
                "LOGGED_IN"
        );
        response.setName(salesPerson.getFirstName() + " " + salesPerson.getLastName());
        return response;
    }

    private RoleType convertSalesRoleToRoleType(SalesRole salesRole) {
        switch (salesRole) {
            case NATIONAL_SALES_MGR:
                return RoleType.NATIONAL_SALES_MGR;
            case STATE_SALES_MGR:
                return RoleType.STATE_SALES_MGR;
            case ZONAL_SALES_MGR:
                return RoleType.ZONAL_SALES_MGR;
            case REGIONAL_SALES_MGR:
                return RoleType.REGIONAL_SALES_MGR;
            case AREA_SALES_MGR:
                return RoleType.AREA_SALES_MGR;
            case SALES_OFFICER:
                return RoleType.SALES_OFFICER;
            case SALES_EXECUTIVE:
                return RoleType.SALES_EXECUTIVE;
            default:
                return RoleType.SALES_EXECUTIVE;
        }
    }

    private LoginResponse authenticateSalesPerson(User user, LoginRequest request) {

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Username inactive");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // String token = jwtService.generateToken(request.getUsername()); // JWT disabled
        String token = null;

        user.setLoggedIn(true);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        RoleType roleType = user.getRoleType() != null && user.getRoleType().name().contains("SALES")
                ? user.getRoleType()
                : user.getRoles().stream()
                    .map(Role::getRoleType)
                    .filter(rt -> rt.name().contains("SALES"))
                    .findFirst()
                    .orElse(RoleType.SALES_EXECUTIVE);

        List<Object> featureDetails = getSalesPersonFeatures(roleType);
        Set<String> featureNames = getSalesPersonFeatureNames(roleType);

        return new LoginResponse(
                token,
                "Bearer",
                request.getUsername(),
                "Login successful for Salesperson " + request.getUsername(),
                roleType.name(),
                user.getId(),
                featureDetails,
                featureNames,
                "LOGGED_IN"
        );
    }


    private LoginResponse authenticateNormalUser(User user, LoginRequest request) {

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Username inactive, Contact ADMIN");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // String token = jwtService.generateToken(request.getUsername()); // JWT disabled
        String token = null;

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
            features = Arrays.stream(Features.values())
                    .collect(Collectors.toSet());
            log.info("[DEBUG] Admin user - all features: {}", features.size());
        } else {
            // Fetch features from role_feature_permissions table by role ID (1–21)
            features = new HashSet<>();

            Integer roleId = RoleFeatureMapping.getRoleId(user.getRoleType());
            log.info("[DEBUG] Querying role_feature_permissions by roleId: {} for user: {}", roleId, user.getId());
            List<RoleFeaturePermission> permissions = roleFeaturePermissionRepository.findByRoleId(roleId);
            log.info("[DEBUG] Found {} permissions for userId: {}", permissions.size(), roleId);
            
            // Filter to only include features where at least one of create/read/update is true
            // Ignore features that only have delete permission
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
                .map(feature -> Map.of(
                        "name", feature.name(),
                        "displayName", feature.getDisplayName(),
                        "path", feature.getPath()))
                .collect(Collectors.toList());

        Set<String> featureNames =
                features.stream().map(Features::name).collect(Collectors.toSet());

        return new LoginResponse(
                token,
                "Bearer",
                request.getUsername(),
                "Login successful for User " + request.getUsername(),
                user.getRoleType().name(),
                user.getId(),
                featureDetails,
                featureNames,
                "LOGGED_IN"
        );
    }

    private DistributorLoginResponse authenticateDistributor(
            Distributor user, LoginRequest request) {

        if (user.getStatus() != DistributorStatus.ACTIVE) {
            log.warn("Username inactive: {}", request.getUsername());
            throw new RuntimeException("Username inactive, Contact ADMIN");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            log.warn("Invalid password for username: {}", request.getUsername());
            throw new RuntimeException("Invalid password");
        }

        // String token = jwtService.generateToken(request.getUsername()); // JWT disabled
        String token = null;

        // Distributor features
        List<Object> featureDetails = List.of(
                Map.of("displayName", "OrderDetails", "name", "ORDER_DETAILS", "path", "/order-details"),
                Map.of("displayName", "Dashboard", "name", "DASHBOARD", "path", "/dashboard"),
                Map.of("displayName", "Reports", "name", "REPORTS", "path", "/reports"),
                Map.of("displayName", "Products", "name", "PRODUCTS", "path", "/products"),
                Map.of("displayName", "Complaint", "name", "COMPLAINT", "path", "/complaint"),
                Map.of("displayName", "PlaceOrder", "name", "PLACE_ORDER", "path", "/placeOrder")
        );

        Set<String> featureNames = Set.of("ORDER_DETAILS", "PRODUCTS", "DASHBOARD", "REPORTS", "COMPLAINT","PLACE_ORDER");

        log.info(" DISTRUBTOR LOGGED IN .......", request.getUsername());

        return new DistributorLoginResponse(
                token,
                "Bearer",
                request.getUsername(),
                "Login successful for Distributor " + request.getUsername(),
                user.getId(),
                "distributor",
                featureDetails,
                featureNames,
                user.getFirstName(),
                user.getDistributorCode(),
                "LOGGED_IN"
        );
    }

    private boolean isSalesPerson(User user) {
        Set<RoleType> salesRoles = Set.of(
                RoleType.NATIONAL_SALES_MGR,
                RoleType.STATE_SALES_MGR,
                RoleType.ZONAL_SALES_MGR,
                RoleType.REGIONAL_SALES_MGR,
                RoleType.AREA_SALES_MGR,
                RoleType.SALES_OFFICER,
                RoleType.SALES_EXECUTIVE
        );

        return salesRoles.contains(user.getRoleType()) ||
               user.getRoles().stream()
                .map(Role::getRoleType)
                .anyMatch(salesRoles::contains);
    }

    private List<Object> getDistributorFeatures() {
        return List.of(
                Map.of("displayName", "OrderDetails", "name", "ORDER_DETAILS", "path", "/order-details"),
                Map.of("displayName", "Dashboard", "name", "DASHBOARD", "path", "/dashboard"),
                Map.of("displayName", "Reports", "name", "REPORTS", "path", "/reports"),
                Map.of("displayName", "Products", "name", "PRODUCTS", "path", "/products"),
                Map.of("displayName", "Complaint", "name", "COMPLAINT", "path", "/complaint")
        );
    }

    private Set<String> getDistributorFeatureNames() {
        return Set.of("ORDER_DETAILS", "PRODUCTS", "DASHBOARD", "REPORTS", "COMPLAINT");
    }

    private List<Object> getSalesPersonFeatures(RoleType roleType) {
        return List.of(
                Map.of("displayName", "Dashboard", "name", "DASHBOARD", "path", "/dashboard"),
                Map.of("displayName", "OrderDetails", "name", "ORDER_DETAILS", "path", "/order-details"),
                Map.of("displayName", "Products", "name", "PRODUCTS", "path", "/products"),
                Map.of("displayName", "Reports", "name", "REPORTS", "path", "/reports"),
                Map.of("displayName", "Analytics", "name", "ANALYTICS", "path", "/analytics"),
                Map.of("displayName", "Sales", "name", "SALES", "path", "/sales")
        );
    }

    private Set<String> getSalesPersonFeatureNames(RoleType roleType) {
        return Set.of("DASHBOARD", "ORDER_DETAILS", "SALES", "REPORTS", "ANALYTICS","COMPLAINT");
    }



    @Override
    public BaseLoginResponse authenticateWithPermissions(LoginRequest request) {
        log.info("Unified login attempt for {}", request.getUsername());


        // =========================================================
        // 1️⃣ DISTRIBUTOR LOGIN FIRST
        // =========================================================
        Optional<Distributor> distOpt =
                distributorRepository.findByUsername(request.getUsername());

        if (distOpt.isPresent()) {
            log.info("Distributor login detected for {}", request.getUsername());
            return authenticateDistributor(distOpt.get(), request);
        }

        // =========================================================
        // 2️⃣ SALESPERSON LOGIN CHECK
        // =========================================================
        Optional<SalesPerson> salesPersonOpt =
                salesPersonRepository.findByUsername(request.getUsername());

        if (salesPersonOpt.isPresent()) {
            log.info("Salesperson login detected for {}", request.getUsername());
            return authenticateSalesPersonFromTable(salesPersonOpt.get(), request);
        }

        // First authenticate the user normally
        BaseLoginResponse baseResponse = authenticate(request);
        
        if (!(baseResponse instanceof LoginResponse)) {
            throw new RuntimeException("Unified login only supports regular users");
        }
        
        LoginResponse loginResponse = (LoginResponse) baseResponse;
        
        // Get the user's role ID using the same logic as RoleFeaturePermissionController
        User user = userRepository.findByUsernameWithRolesAndPermissions(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<FeaturePermissionDTO> rolePermissions = getUserPermissions(user);

        log.info(" USER ON WEB LOGGED IN .......", request.getUsername());

        return new UnifiedLoginResponse(
                loginResponse.getToken(),
                loginResponse.getType(),
                loginResponse.getUsername(),
                loginResponse.getMessage(),
                loginResponse.getRoleType(),
                loginResponse.getUserId(),
                loginResponse.getFeatures(),
                loginResponse.getFeatureNames(),
                loginResponse.getLoginStatus(),
                rolePermissions
        );
    }

    /**
     * Get user permissions based on userId - queries role_feature_permissions table
     * Note: Database stores userId as role_id
     * Only returns permissions where at least one CRUD operation is allowed
     */
    private List<FeaturePermissionDTO> getUserPermissions(User user) {
        Integer roleId = RoleFeatureMapping.getRoleId(user.getRoleType());
        log.info("[DEBUG] getUserPermissions - querying by roleId: {} for user: {}", roleId, user.getId());

        return roleFeaturePermissionRepository.findByRoleId(roleId)
                .stream()
                .filter(perm -> Boolean.TRUE.equals(perm.getCanCreate()) || 
                               Boolean.TRUE.equals(perm.getCanRead()) || 
                               Boolean.TRUE.equals(perm.getCanUpdate()))
                .map(perm -> new FeaturePermissionDTO(
                        perm.getRoleId(),
                        perm.getFeatureId(),
                        perm.getFeature().name(),
                        perm.getCanCreate(),
                        perm.getCanRead(),
                        perm.getCanUpdate(),
                        perm.getCanDelete()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Get permissions by role ID and convert to DTO - same logic as RoleFeaturePermissionController
     */
    private List<FeaturePermissionDTO> getPermissionsByRoleId(Integer roleId) {
        return roleFeaturePermissionRepository.findByRoleId(roleId)
                .stream()
                .map(perm -> new FeaturePermissionDTO(
                        perm.getRoleId(),
                        perm.getFeatureId(),
                        perm.getFeature().name(),
                        perm.getCanCreate(),
                        perm.getCanRead(),
                        perm.getCanUpdate(),
                        perm.getCanDelete()
                ))
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
