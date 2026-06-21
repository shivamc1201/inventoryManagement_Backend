package com.nector.userservice.ledger.repository;

import com.nector.userservice.ledger.entity.LedgerTransaction;
import com.nector.userservice.ledger.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LedgerTransaction entity
 * Provides data access methods for ledger transactions
 */
@Repository
public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {

    /**
     * Find all transactions for a ledger account
     */
    Page<LedgerTransaction> findByLedgerAccountIdOrderByCreatedAtDesc(Long ledgerAccountId, Pageable pageable);

    /**
     * Find transactions by ledger account and date range
     */
    @Query("SELECT lt FROM LedgerTransaction lt WHERE lt.ledgerAccount.id = :accountId " +
           "AND lt.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY lt.transactionDate DESC, lt.createdAt DESC")
    Page<LedgerTransaction> findByAccountAndDateRange(@Param("accountId") Long accountId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     Pageable pageable);

    /**
     * Find transactions by type
     */
    Page<LedgerTransaction> findByLedgerAccountIdAndTransactionTypeOrderByCreatedAtDesc(
            Long ledgerAccountId, TransactionType transactionType, Pageable pageable);

    /**
     * Search transactions by reference or description
     */
    @Query("SELECT lt FROM LedgerTransaction lt WHERE lt.ledgerAccount.id = :accountId " +
           "AND (LOWER(lt.referenceNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(lt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY lt.createdAt DESC")
    Page<LedgerTransaction> searchTransactions(@Param("accountId") Long accountId,
                                             @Param("searchTerm") String searchTerm,
                                             Pageable pageable);

    /**
     * Find transaction by reference number
     */
    Optional<LedgerTransaction> findByReferenceNumber(String referenceNumber);

    /**
     * Calculate total debits for account
     */
    @Query("SELECT COALESCE(SUM(lt.debitAmount), 0) FROM LedgerTransaction lt " +
           "WHERE lt.ledgerAccount.id = :accountId")
    BigDecimal calculateTotalDebits(@Param("accountId") Long accountId);

    /**
     * Calculate total credits for account
     */
    @Query("SELECT COALESCE(SUM(lt.creditAmount), 0) FROM LedgerTransaction lt " +
           "WHERE lt.ledgerAccount.id = :accountId")
    BigDecimal calculateTotalCredits(@Param("accountId") Long accountId);

    /**
     * Calculate totals for date range
     */
    @Query("SELECT COALESCE(SUM(lt.debitAmount), 0), COALESCE(SUM(lt.creditAmount), 0) " +
           "FROM LedgerTransaction lt WHERE lt.ledgerAccount.id = :accountId " +
           "AND lt.transactionDate BETWEEN :startDate AND :endDate")
    Object[] calculateTotalsForDateRange(@Param("accountId") Long accountId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * Get last transaction for account
     */
    @Query("SELECT lt FROM LedgerTransaction lt WHERE lt.ledgerAccount.id = :accountId " +
           "ORDER BY lt.createdAt DESC LIMIT 1")
    Optional<LedgerTransaction> findLastTransactionByAccountId(@Param("accountId") Long accountId);

    /**
     * Check if opening balance exists
     */
    boolean existsByLedgerAccountIdAndTransactionType(Long ledgerAccountId, TransactionType transactionType);

    /**
     * Count transactions for account
     */
    long countByLedgerAccountId(Long ledgerAccountId);

    /**
     * Find transactions for export (no pagination)
     */
    @Query("SELECT lt FROM LedgerTransaction lt WHERE lt.ledgerAccount.id = :accountId " +
           "AND (:startDate IS NULL OR lt.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR lt.transactionDate <= :endDate) " +
           "AND (:transactionType IS NULL OR lt.transactionType = :transactionType) " +
           "ORDER BY lt.transactionDate DESC, lt.createdAt DESC")
    List<LedgerTransaction> findTransactionsForExport(@Param("accountId") Long accountId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     @Param("transactionType") TransactionType transactionType);
}