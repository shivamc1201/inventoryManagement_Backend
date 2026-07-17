package com.nector.userservice.interceptors.userCreate.impl;

import com.nector.userservice.enums.UserOnboardingType;
import com.nector.userservice.exception.UsernameAlreadyExistsException;
import com.nector.userservice.interceptors.userCreate.model.UserRequest;
import com.nector.userservice.interceptors.userCreate.model.UserResponse;
import com.nector.userservice.interceptors.userCreate.service.UserService;
import com.nector.userservice.model.PendingOnboardingRequest;
import com.nector.userservice.repository.PendingOnboardingRequestRepository;
import com.nector.userservice.repository.SalesPersonRepository;
import com.nector.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final PendingOnboardingRequestRepository pendingRepo;

    @Override
    @Transactional
    public UserResponse registerNewUser(UserRequest request) {
        log.info("Entering registerNewUser() - username: {}, type: {}", request.getUsername(), request.getUserOnboardingType());

        validateTypeSpecificFields(request);
        validateUniqueness(request);

        PendingOnboardingRequest pending = new PendingOnboardingRequest();
        pending.setUserOnboardingType(request.getUserOnboardingType());
        pending.setUsername(request.getUsername());
        pending.setEmail(request.getEmail());
        pending.setPassword(request.getPassword());
        pending.setStatus(request.getStatus());
        pending.setFirstName(request.getFirstName());
        pending.setLastName(request.getLastName());
        pending.setContactNo(request.getContactNo());
        pending.setAlternateContactNo(request.getAlternateContactNo());
        pending.setBloodGroup(request.getBloodGroup());
        pending.setCompleteAddress(request.getCompleteAddress());
        pending.setCity(request.getCity());
        pending.setCountry(request.getCountry());
        pending.setZip(request.getZip());
        pending.setDateOfBirth(request.getDateOfBirth());
        pending.setGender(request.getGender());
        pending.setEmployeeRollNo(request.getEmployeeRollNo());

        if (request.getUserOnboardingType() == UserOnboardingType.USER) {
            pending.setRoleType(request.getRoleType());
        } else {
            pending.setSalesRole(request.getSalesRole());
            pending.setZone(request.getZone());
            pending.setRegion(request.getRegion());
        }

        PendingOnboardingRequest saved = pendingRepo.save(pending);
        log.info("Exiting registerNewUser() - pendingRequestId: {}, type: {}", saved.getId(), saved.getUserOnboardingType());

        return buildResponse(saved);
    }

    private void validateTypeSpecificFields(UserRequest request) {
        if (request.getUserOnboardingType() == UserOnboardingType.USER) {
            if (request.getRoleType() == null || request.getRoleType().isBlank()) {
                throw new IllegalArgumentException("roleType is required for USER onboarding type");
            }
        } else if (request.getUserOnboardingType() == UserOnboardingType.SALES) {
            if (request.getSalesRole() == null) {
                throw new IllegalArgumentException("salesRole is required for SALES onboarding type");
            }
        }
    }

    private void validateUniqueness(UserRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();

        // Check against already-approved users
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already taken: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new UsernameAlreadyExistsException("Email already registered: " + email);
        }

        // Check against already-approved sales persons
        if (salesPersonRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already taken: " + username);
        }
        if (salesPersonRepository.existsByEmail(email)) {
            throw new UsernameAlreadyExistsException("Email already registered: " + email);
        }

        // Check against currently pending requests
        if (pendingRepo.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("A pending request already exists for username: " + username);
        }
        if (pendingRepo.existsByEmail(email)) {
            throw new UsernameAlreadyExistsException("A pending request already exists for email: " + email);
        }
    }

    private UserResponse buildResponse(PendingOnboardingRequest saved) {
        UserResponse response = new UserResponse();
        response.setId(saved.getId());
        response.setUsername(saved.getUsername());
        response.setEmail(saved.getEmail());
        response.setFirstName(saved.getFirstName());
        response.setLastName(saved.getLastName());
        response.setStatus(saved.getStatus());
        response.setContactNo(saved.getContactNo());
        response.setAlternateContactNo(saved.getAlternateContactNo());
        response.setBloodGroup(saved.getBloodGroup());
        response.setCompleteAddress(saved.getCompleteAddress());
        response.setGender(saved.getGender());
        response.setCity(saved.getCity());
        response.setCountry(saved.getCountry());
        response.setZip(saved.getZip());
        response.setDateOfBirth(saved.getDateOfBirth());
        response.setEmployeeRollNo(saved.getEmployeeRollNo());
        response.setRoleType(saved.getRoleType());
        response.setUserOnboardingType(saved.getUserOnboardingType());
        return response;
    }
}
