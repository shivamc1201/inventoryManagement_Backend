package com.nector.userservice.repository;

import com.nector.userservice.model.PaymentApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentApprovalRepository extends JpaRepository<PaymentApproval, Long> {
    List<PaymentApproval> findByStatusOrderByCreatedAtDesc(String status);
    List<PaymentApproval> findByDistributorIdOrderByCreatedAtDesc(Long distributorId);
}
