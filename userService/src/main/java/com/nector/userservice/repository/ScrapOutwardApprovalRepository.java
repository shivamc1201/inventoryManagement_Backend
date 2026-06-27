package com.nector.userservice.repository;

import com.nector.userservice.model.ScrapOutwardApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapOutwardApprovalRepository extends JpaRepository<ScrapOutwardApproval, Long> {

    List<ScrapOutwardApproval> findByApprovalStatus(String approvalStatus);
}
