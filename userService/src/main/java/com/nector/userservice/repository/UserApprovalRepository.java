package com.nector.userservice.repository;

import com.nector.userservice.model.UserApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserApprovalRepository extends JpaRepository<UserApproval, Long> {
    List<UserApproval> findByApprovalStatus(String approvalStatus);
    UserApproval findByUserId(Long userId);
}