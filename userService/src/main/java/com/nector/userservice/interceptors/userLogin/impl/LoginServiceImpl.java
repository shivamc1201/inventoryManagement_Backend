package com.nector.userservice.interceptors.userLogin.impl;

import com.nector.userservice.common.BaseLoginResponse;
import com.nector.userservice.common.RoleType;
import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.interceptors.distributor.model.Distributor;
import com.nector.userservice.interceptors.distributor.model.DistributorLoginResponse;
import com.nector.userservice.interceptors.distributor.model.DistributorStatus;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;
import com.nector.userservice.interceptors.userLogin.service.LoginService;
import com.nector.userservice.model.Role;
import com.nector.userservice.model.User;
// import com.nector.userservice.model.UserSession;
import com.nector.userservice.repository.UserRepository;
// import com.nector.userservice.repository.UserSessionRepository;
import com.nector.userservice.service.JwtService;
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
    private final JwtService jwtService;
    private final DistributorRepository distributorRepository;
    
//    @Override
//     @Transactional // For future session management
//    public LoginResponse authenticateUser(LoginRequest request) {
//        log.info("Entering authenticateUser() for username: {}", request.getUsername());
//
//        User user = userRepository.findByUsername(request.getUsername())
//            .orElse(null);
//
//        if (user == null) {
//            log.warn("Exiting authenticateUser() - Username not available: {}", request.getUsername());
//            throw new RuntimeException("Username not available");
//        }
//
//        if (user.getStatus() != UserStatus.ACTIVE) {
//            log.warn("Exiting authenticateUser() - Username inactive: {}", request.getUsername());
//            throw new RuntimeException("Username inactive, Contact ADMIN");
//        }
//
//        // Force logout previous session if user is already logged in
//        if (user.isLoggedIn()) {
//            log.warn("User already logged in, forcing logout previous session: {}", request.getUsername());
//            user.setLoggedIn(false);
//        }
//
//        if (!user.getPassword().equals(request.getPassword())) {
//            log.warn("Exiting authenticateUser() - Invalid password for username: {}", request.getUsername());
//            throw new RuntimeException("Invalid password");
//        }
//
//        // Get user with roles and permissions
//        Optional<User> userWithRoles = userRepository.findByUsernameWithRolesAndPermissions(request.getUsername());
//        if (userWithRoles.isPresent()) {
//            user = userWithRoles.get();
//            log.info("User loaded with {} roles", user.getRoles().size());
//        } else {
//            log.warn("User {} found but has no roles assigned", request.getUsername());
//        }
//
//        String token = jwtService.generateToken(request.getUsername());
//
//        /* FUTURE SESSION MANAGEMENT CODE - UNCOMMENT WHEN NEEDED
//        // Create new session
//        UserSession session = new UserSession();
//        session.setUserId(user.getId());
//        session.setSessionToken(token);
//        session.setLoginTime(LocalDateTime.now());
//        session.setLastActivity(LocalDateTime.now());
//        userSessionRepository.save(session);
//        */
//
//        // Update user login status and last login time
//        user.setLoggedIn(true);
//        user.setLastLoginTime(LocalDateTime.now());
//        userRepository.save(user);
//
//        Set<Features> features;
//        boolean isAdmin = user.getRoles().stream()
//                .anyMatch(role -> "ADMIN".equals(role.getName()));
//
//        if (isAdmin) {
//            features = Set.of(Features.values());
//            log.info("ADMIN role detected - assigned all {} features", features.size());
//        } else {
//            features = user.getRoles().stream()
//                    .flatMap(role -> {
//                        log.info("Role: {} has {} permissions", role.getName(), role.getPermissions().size());
//                        return role.getPermissions().stream();
//                    })
//                    .map(permission -> permission.getFeature())
//                    .collect(Collectors.toSet());
//            log.info("Total unique features extracted: {}", features.size());
//        }
//
//
//        List<Object> featureDetails = features.stream()
//            .map(feature -> Map.of(
//                "name", feature.name(),
//                "displayName", feature.getDisplayName(),
//                "path", feature.getPath()
//            ))
//            .collect(Collectors.toList());
//
//        Set<String> featureNames = features.stream()
//            .map(Features::name)
//            .collect(Collectors.toSet());
//
//        LoginResponse response = new LoginResponse(
//            token,
//            "Bearer",
//            request.getUsername(),
//            "Login successful for " + request.getUsername(),
//            user.getRoleType().name(),
//            user.getId(),
//            featureDetails,
//            featureNames,
//            "LOGGED_IN"
//        );
//
//        log.info("Exiting authenticateUser() - Login successful for username: {}", request.getUsername());
//        return response;
//    }

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
        // 2️⃣ USER LOGIN (SALES OR NORMAL)
        // =========================================================
        Optional<User> userOpt =
                userRepository.findByUsername(request.getUsername());

        if (userOpt.isPresent()) {

            User user = userOpt.get();

            // Load roles
            user = userRepository
                    .findByUsernameWithRolesAndPermissions(request.getUsername())
                    .orElse(user);

            if (isSalesPerson(user)) {
                log.info("Salesperson login detected for {}", request.getUsername());
                return authenticateSalesPerson(user, request);
            }

            return authenticateNormalUser(user, request);
        }

        // =========================================================
        // 3️⃣ USER NOT FOUND
        // =========================================================
        log.warn("Username not found {}", request.getUsername());
        throw new RuntimeException("Username not available");
    }

    private LoginResponse authenticateSalesPerson(User user, LoginRequest request) {

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Username inactive");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(request.getUsername());

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

        String token = jwtService.generateToken(request.getUsername());

        user.setLoggedIn(true);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        Set<Features> features;
        boolean isAdmin = user.getRoleType() == RoleType.ADMIN || 
                          user.getRoleType() == RoleType.SUPER_ADMIN ||
                          user.getRoles().stream()
                              .map(Role::getRoleType)
                              .anyMatch(rt -> rt == RoleType.ADMIN || rt == RoleType.SUPER_ADMIN);

        if (isAdmin) {
            features = Arrays.stream(Features.values())
                    .filter(feature -> feature != Features.SALES)
                    .collect(Collectors.toSet());
        } else {
            features = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> permission.getFeature())
                    .collect(Collectors.toSet());
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

        String token = jwtService.generateToken(request.getUsername());

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

        return new DistributorLoginResponse(
                token,
                "Bearer",
                request.getUsername(),
                "Login successful for Distributor " + request.getUsername(),
                user.getId(),
                "distributor",
                featureDetails,
                featureNames,
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
        List<Object> baseFeatures = new ArrayList<>(List.of(
                Map.of("displayName", "Dashboard", "name", "DASHBOARD", "path", "/dashboard"),
                Map.of("displayName", "OrderDetails", "name", "ORDER_DETAILS", "path", "/order-details"),
                Map.of("displayName", "Products", "name", "PRODUCTS", "path", "/products"),
                Map.of("displayName", "Sales", "name", "SALES", "path", "/sales")
        ));

        if (roleType == RoleType.NATIONAL_SALES_MGR || roleType == RoleType.STATE_SALES_MGR || roleType == RoleType.ZONAL_SALES_MGR) {
            baseFeatures.add(Map.of("displayName", "Reports", "name", "REPORTS", "path", "/reports"));
            baseFeatures.add(Map.of("displayName", "Analytics", "name", "ANALYTICS", "path", "/analytics"));
        }

        return baseFeatures;
    }

    private Set<String> getSalesPersonFeatureNames(RoleType roleType) {
        Set<String> baseFeatures = new HashSet<>(Set.of("DASHBOARD", "ORDER_DETAILS", "PRODUCTS"));

        if (roleType == RoleType.NATIONAL_SALES_MGR || roleType == RoleType.STATE_SALES_MGR || roleType == RoleType.ZONAL_SALES_MGR) {
            baseFeatures.add("REPORTS");
            baseFeatures.add("ANALYTICS");
        }

        return baseFeatures;
    }



    @Override
    public LoginResponse authenticateSecondUser(LoginRequest request) {
        log.info("Entering authenticateSecondUser() for username: {}", request.getUsername());

        return null;
    }
}