package com.nector.userservice.repository;

import com.nector.userservice.model.DistributorLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DistributorLedgerRepository extends JpaRepository<DistributorLedger, Long> {
    
    List<DistributorLedger> findByDistributorIdOrderByCreatedAtDesc(Long distributorId);
    
    @Query("SELECT COALESCE(SUM(CASE WHEN dl.transactionType = 'CREDIT' THEN dl.amount ELSE -dl.amount END), 0) FROM DistributorLedger dl WHERE dl.distributorId = ?1")
    BigDecimal getDistributorBalance(Long distributorId);
}