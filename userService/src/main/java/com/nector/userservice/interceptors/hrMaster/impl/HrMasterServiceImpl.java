package com.nector.userservice.interceptors.hrMaster.impl;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.enums.UserOnboardingType;
import com.nector.userservice.interceptors.hrMaster.model.ApprovalRequest;
import com.nector.userservice.interceptors.hrMaster.model.PendingUserEditRequest;
import com.nector.userservice.interceptors.hrMaster.service.HrMasterService;
import com.nector.userservice.model.PendingOnboardingRequest;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.PendingOnboardingRequestRepository;
import com.nector.userservice.repository.SalesPersonRepository;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrMasterServiceImpl implements HrMasterService {

    private final PendingOnboardingRequestRepository pendingRepo;
    private final UserRepository userRepository;
    private final SalesPersonRepository salesPersonRepository;

    @Override
    public List<PendingOnboardingRequest> getPendingApprovals() {
        log.info("Fetching all pending onboarding requests");
        return pendingRepo.findByApprovalStatus("PENDING");
    }

    @Override
    @Transactional
    public String processApproval(ApprovalRequest request, String reviewedByUsername) {
        log.info("processApproval() - pendingRequestId: {}, action: {}", request.getPendingRequestId(), request.getAction());

        PendingOnboardingRequest pending = pendingRepo.findById(request.getPendingRequestId())
                .orElseThrow(() -> new RuntimeException("Pending request not found: " + request.getPendingRequestId()));

        if (!"PENDING".equals(pending.getApprovalStatus())) {
            throw new RuntimeException("Request is already " + pending.getApprovalStatus());
        }

        String action = request.getAction() != null ? request.getAction().toUpperCase() : "";
        if ("APPROVE".equals(action)) {
            if (pending.getUserOnboardingType() == UserOnboardingType.USER) {
                saveAsUser(pending);
            } else {
                saveAsSalesPerson(pending);
            }
            pending.setApprovalStatus("APPROVED");
        } else if ("REJECT".equals(action)) {
            pending.setApprovalStatus("REJECTED");
        } else {
            throw new RuntimeException("Invalid action. Must be APPROVE or REJECT");
        }

        pending.setReviewedBy(reviewedByUsername);
        pending.setReviewedOn(LocalDateTime.now());
        pending.setReviewComments(request.getComments());
        pendingRepo.save(pending);

        log.info("processApproval() done - pendingRequestId: {}, action: {}", request.getPendingRequestId(), action);
        return "Request " + action.toLowerCase() + "d successfully";
    }

    @Override
    @Transactional
    public PendingOnboardingRequest editPendingUser(Long pendingRequestId, PendingUserEditRequest request) {
        log.info("editPendingUser() - pendingRequestId: {}", pendingRequestId);

        PendingOnboardingRequest pending = pendingRepo.findById(pendingRequestId)
                .orElseThrow(() -> new RuntimeException("Pending request not found: " + pendingRequestId));

        if (!"PENDING".equals(pending.getApprovalStatus())) {
            throw new RuntimeException("Can only edit PENDING requests");
        }

        if (request.getFirstName() != null)          pending.setFirstName(request.getFirstName());
        if (request.getLastName() != null)           pending.setLastName(request.getLastName());
        if (request.getEmail() != null)              pending.setEmail(request.getEmail());
        if (request.getContactNo() != null)          pending.setContactNo(request.getContactNo());
        if (request.getAlternateContactNo() != null) pending.setAlternateContactNo(request.getAlternateContactNo());
        if (request.getBloodGroup() != null)         pending.setBloodGroup(request.getBloodGroup());
        if (request.getCompleteAddress() != null)    pending.setCompleteAddress(request.getCompleteAddress());
        if (request.getCity() != null)               pending.setCity(request.getCity());
        if (request.getCountry() != null)            pending.setCountry(request.getCountry());
        if (request.getZip() != null)                pending.setZip(request.getZip());
        if (request.getDateOfBirth() != null)        pending.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null)             pending.setGender(request.getGender());
        if (request.getEmployeeRollNo() != null)     pending.setEmployeeRollNo(request.getEmployeeRollNo());

        if (UserOnboardingType.SALES.equals(pending.getUserOnboardingType())) {
            if (request.getZone() != null)   pending.setZone(request.getZone());
            if (request.getRegion() != null) pending.setRegion(request.getRegion());
        }

        return pendingRepo.save(pending);
    }

    @Override
    public List<User> getAllSalespersons() {
        return userRepository.findByRoleTypeIn(List.of(
                "NATIONAL_SALES_MGR", "STATE_SALES_MGR", "ZONAL_SALES_MGR",
                "REGIONAL_SALES_MGR", "AREA_SALES_MGR", "SALES_OFFICER", "SALES_EXECUTIVE"
        ));
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private void saveAsUser(PendingOnboardingRequest pending) {
        User user = new User();
        user.setUsername(pending.getUsername());
        user.setEmail(pending.getEmail());
        user.setPassword(pending.getPassword());
        user.setStatus(UserStatus.ACTIVE);
        user.setFirstName(pending.getFirstName());
        user.setLastName(pending.getLastName());
        user.setContactNo(pending.getContactNo());
        user.setAlternateContactNo(pending.getAlternateContactNo());
        user.setBloodGroup(pending.getBloodGroup());
        user.setCompleteAddress(pending.getCompleteAddress());
        user.setCity(pending.getCity());
        user.setCountry(pending.getCountry());
        user.setZip(pending.getZip());
        user.setDateOfBirth(pending.getDateOfBirth());
        user.setGender(pending.getGender());
        user.setEmployeeRollNo(pending.getEmployeeRollNo());
        user.setRoleType(pending.getRoleType());
        user.setUserOnboardingType(UserOnboardingType.USER);
        user.setOtp("1234");
        userRepository.save(user);
        log.info("User saved from pendingRequestId: {}", pending.getId());
    }

    private void saveAsSalesPerson(PendingOnboardingRequest pending) {
        SalesPerson sp = new SalesPerson();
        sp.setFirstName(pending.getFirstName());
        sp.setLastName(pending.getLastName());
        sp.setUsername(pending.getUsername());
        sp.setPassword(pending.getPassword());
        sp.setEmail(pending.getEmail());
        sp.setStatus(UserStatus.ACTIVE);
        sp.setContactNo(pending.getContactNo());
        sp.setAlternateContactNo(pending.getAlternateContactNo());
        sp.setBloodGroup(pending.getBloodGroup());
        sp.setCompleteAddress(pending.getCompleteAddress());
        sp.setCity(pending.getCity());
        sp.setCountry(pending.getCountry());
        sp.setZip(pending.getZip());
        sp.setDateOfBirth(pending.getDateOfBirth());
        sp.setGender(pending.getGender());
        sp.setEmployeeRollNo(pending.getEmployeeRollNo());
        sp.setRole(pending.getSalesRole());
        sp.setZone(pending.getZone());
        sp.setRegion(pending.getRegion());
        sp.setPhone(pending.getContactNo());

        if (pending.getSalesRole() != SalesRole.NATIONAL_SALES_MGR) {
            salesPersonRepository
                    .findFirstByRoleAndActiveTrueOrderByIdAsc(SalesRole.NATIONAL_SALES_MGR)
                    .ifPresent(nsm -> sp.setManagerId(nsm.getId()));
        }

        salesPersonRepository.save(sp);
        log.info("SalesPerson saved from pendingRequestId: {}, role: {}", pending.getId(), pending.getSalesRole());
    }
}
