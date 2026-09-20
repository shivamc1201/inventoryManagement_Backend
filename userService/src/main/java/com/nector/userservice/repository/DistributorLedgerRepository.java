package com.nector.userservice.repository;

import com.nector.userservice.model.DistributorLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DistributorLedgerRepository extends JpaRepository<DistributorLedger, Long> {

    long deleteByDistributorId(Long distributorId);
    
    List<DistributorLedger> findByDistributorIdOrderByCreatedAtDesc(Long distributorId);
    
    List<DistributorLedger> findByDistributorIdAndTransactionTypeOrderByCreatedAtDesc(Long distributorId, String transactionType);

    List<DistributorLedger> findByDistributorIdAndTransactionTypeOrderByCreatedAtAsc(Long distributorId, String transactionType);

    List<DistributorLedger> findByDistributorIdAndTransactionTypeInOrderByCreatedAtDesc(Long distributorId, List<String> transactionTypes);

    // Ledger balance is a distinct account from the credit line.
    // Rows whose description contains '(using credit)' or '(Credit Restored)' belong to the
    // credit-line account and must NOT contribute to the ledger balance sum.
    @Query("SELECT COALESCE(SUM(CASE WHEN dl.transactionType IN ('CREDIT', 'JV_CREDIT') THEN dl.amount ELSE -dl.amount END), 0) " +
           "FROM DistributorLedger dl " +
           "WHERE dl.distributorId = ?1 " +
           "AND (dl.description IS NULL OR (dl.description NOT LIKE '%(using credit)%' AND dl.description NOT LIKE '%(Credit Restored)%'))")
    BigDecimal getDistributorBalance(Long distributorId);
}