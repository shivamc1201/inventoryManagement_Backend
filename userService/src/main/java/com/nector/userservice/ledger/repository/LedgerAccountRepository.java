package com.nector.userservice.ledger.repository;

import com.nector.userservice.ledger.entity.LedgerAccount;
import com.nector.userservice.ledger.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LedgerAccount entity
 * Provides data access methods for ledger accounts
 */
@Repository
public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, Long> {

    /**
     * Find ledger account by company and distributor ID
     */
    Optional<LedgerAccount> findByCompanyIdAndDistributorId(Long companyId, Long distributorId);

    /**
     * Find ledger account by account number
     */
    Optional<LedgerAccount> findByAccountNumber(String accountNumber);

    /**
     * Find ledger account with pessimistic lock for balance updates
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT la FROM LedgerAccount la WHERE la.id = :id")
    Optional<LedgerAccount> findByIdWithLock(@Param("id") Long id);

    /**
     * Find all accounts by company ID
     */
    List<LedgerAccount> findByCompanyId(Long companyId);

    /**
     * Find all accounts by status
     */
    List<LedgerAccount> findByStatus(AccountStatus status);

    /**
     * Find all active accounts by company ID
     */
    @Query("SELECT la FROM LedgerAccount la WHERE la.companyId = :companyId AND la.status = 'ACTIVE'")
    List<LedgerAccount> findActiveAccountsByCompanyId(@Param("companyId") Long companyId);

    /**
     * Check if account exists for company and distributor
     */
    boolean existsByCompanyIdAndDistributorId(Long companyId, Long distributorId);

    /**
     * Check if account number exists
     */
    boolean existsByAccountNumber(String accountNumber);
}