package com.nector.userservice.repository;

import com.nector.userservice.model.DocumentNumberSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface DocumentNumberSequenceRepository extends JpaRepository<DocumentNumberSequence, Long> {
    
    /**
     * Find sequence by document type and financial year with pessimistic lock
     * This ensures thread-safe sequential number generation
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DocumentNumberSequence d WHERE d.documentType = :documentType AND d.financialYear = :financialYear")
    Optional<DocumentNumberSequence> findByDocumentTypeAndFinancialYearWithLock(String documentType, String financialYear);
    
    /**
     * Find sequence without lock (for read-only operations)
     */
    Optional<DocumentNumberSequence> findByDocumentTypeAndFinancialYear(String documentType, String financialYear);
}

