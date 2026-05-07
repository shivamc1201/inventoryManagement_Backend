package com.nector.userservice.repository;

import com.nector.userservice.model.DealerLedgerTransaction;
import com.nector.userservice.enums.LedgerTransactionCategory;
import com.nector.userservice.enums.LedgerTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DealerLedgerTransactionRepository extends JpaRepository<DealerLedgerTransaction, String> {

    long deleteByDistributorId(Long distributorId);

    // Tenant isolation methods
    Optional<DealerLedgerTransaction> findByIdAndDistributorId(String id, Long distributorId);

    List<DealerLedgerTransaction> findByDealerIdAndDistributorIdOrderByDateDescCreatedAtDesc(Long dealerId, Long distributorId);

    List<DealerLedgerTransaction> findByDealerIdAndDistributorIdAndDateBetweenOrderByDateDescCreatedAtDesc(
            Long dealerId, Long distributorId, LocalDate startDate, LocalDate endDate);

    // Reference uniqueness check and lookup
    boolean existsByDealerIdAndDistributorIdAndReference(Long dealerId, Long distributorId, String reference);

    Optional<DealerLedgerTransaction> findByDealerIdAndDistributorIdAndReference(Long dealerId, Long distributorId, String reference);

    // Balance calculation methods with pessimistic locking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dlt FROM DealerLedgerTransaction dlt WHERE dlt.dealerId = :dealerId AND dlt.distributorId = :distributorId " +
           "ORDER BY dlt.date DESC, dlt.createdAt DESC LIMIT 1")
    Optional<DealerLedgerTransaction> findLatestByDealerIdAndDistributorIdForUpdate(
            @Param("dealerId") Long dealerId, @Param("distributorId") Long distributorId);

    @Query("SELECT dlt FROM DealerLedgerTransaction dlt WHERE dlt.dealerId = :dealerId AND dlt.distributorId = :distributorId " +
           "ORDER BY dlt.date DESC, dlt.createdAt DESC LIMIT 1")
    Optional<DealerLedgerTransaction> findLatestByDealerIdAndDistributorId(
            @Param("dealerId") Long dealerId, @Param("distributorId") Long distributorId);

    // Summary statistics
    @Query("SELECT COUNT(dlt) FROM DealerLedgerTransaction dlt WHERE dlt.dealerId = :dealerId AND dlt.distributorId = :distributorId")
    long countTransactionsByDealer(@Param("dealerId") Long dealerId, @Param("distributorId") Long distributorId);

    @Query("SELECT SUM(dlt.debit) FROM DealerLedgerTransaction dlt WHERE dlt.dealerId = :dealerId AND dlt.distributorId = :distributorId")
    BigDecimal sumDebitsByDealer(@Param("dealerId") Long dealerId, @Param("distributorId") Long distributorId);

    @Query("SELECT SUM(dlt.credit) FROM DealerLedgerTransaction dlt WHERE dlt.dealerId = :dealerId AND dlt.distributorId = :distributorId")
    BigDecimal sumCreditsByDealer(@Param("dealerId") Long dealerId, @Param("distributorId") Long distributorId);

    @Query("SELECT dlt.balance FROM DealerLedgerTransaction dlt WHERE dlt.dealerId = :dealerId AND dlt.distributorId = :distributorId " +
           "ORDER BY dlt.date DESC, dlt.createdAt DESC LIMIT 1")
    BigDecimal getLatestBalanceByDealer(@Param("dealerId") Long dealerId, @Param("distributorId") Long distributorId);

    // Category-based queries
    List<DealerLedgerTransaction> findByDealerIdAndDistributorIdAndCategoryOrderByDateDescCreatedAtDesc(
            Long dealerId, Long distributorId, LedgerTransactionCategory category);

    List<DealerLedgerTransaction> findByDealerIdAndDistributorIdAndTypeOrderByDateDescCreatedAtDesc(
            Long dealerId, Long distributorId, LedgerTransactionType type);

    // All transactions for a dealer (any distributor) - used for PDF generation
    List<DealerLedgerTransaction> findByDealerIdOrderByDateAscCreatedAtAsc(Long dealerId);

    // Distributor overview
    @Query("SELECT dlt.dealerId, d.fullName, dlt.balance FROM DealerLedgerTransaction dlt " +
           "JOIN Dealer d ON dlt.dealerId = d.id WHERE dlt.distributorId = :distributorId AND " +
           "dlt.id IN (SELECT MAX(sub.id) FROM DealerLedgerTransaction sub " +
           "WHERE sub.dealerId = dlt.dealerId AND sub.distributorId = :distributorId)")
    List<Object[]> getDistributorOverview(@Param("distributorId") Long distributorId);
}
