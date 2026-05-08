package com.nector.userservice.repository;

import com.nector.userservice.model.PaymentApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentApprovalRepository extends JpaRepository<PaymentApproval, Long> {
    List<PaymentApproval> findByStatusOrderByCreatedAtDesc(String status);
    List<PaymentApproval> findByDistributorIdOrderByCreatedAtDesc(Long distributorId);
    List<PaymentApproval> findByDistributorIdAndStatusOrderByCreatedAtDesc(Long distributorId, String status);
    List<PaymentApproval> findBySalespersonIdAndStatusOrderByCreatedAtDesc(Long salespersonId, String status);
    List<PaymentApproval> findByDistributorIdAndSalespersonIdAndStatusOrderByCreatedAtDesc(Long distributorId, Long salespersonId, String status);

    // Sum approved payments by distributor and date range (for collection calculations)
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentApproval p WHERE p.distributorId = :distributorId AND p.status = 'LEDGER_UPDATED' AND p.approvedAt BETWEEN :startDate AND :endDate")
    BigDecimal sumApprovedPaymentsByDistributorAndDateRange(@Param("distributorId") Long distributorId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
