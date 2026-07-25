package com.nector.userservice.repository;

import com.nector.userservice.model.PendingOnboardingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PendingOnboardingRequestRepository extends JpaRepository<PendingOnboardingRequest, Long> {

    List<PendingOnboardingRequest> findByApprovalStatus(String approvalStatus);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
