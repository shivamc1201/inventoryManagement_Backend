package com.nector.userservice.repository;

import com.nector.userservice.model.OutwardItemTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutwardItemTransactionRepository extends JpaRepository<OutwardItemTransaction, Long> {

    List<OutwardItemTransaction> findByItemType(OutwardItemTransaction.ItemType itemType);

    List<OutwardItemTransaction> findByTransactionType(OutwardItemTransaction.TransactionType transactionType);

    List<OutwardItemTransaction> findByMaterialCode(String materialCode);

    List<OutwardItemTransaction> findByItemTypeAndTransactionType(
            OutwardItemTransaction.ItemType itemType,
            OutwardItemTransaction.TransactionType transactionType);

    // --- Report queries ---

    @Query("SELECT o FROM OutwardItemTransaction o WHERE o.createdAt BETWEEN :from AND :to ORDER BY o.createdAt DESC")
    Page<OutwardItemTransaction> findByDateRange(@Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to,
                                                  Pageable pageable);

    @Query("SELECT o FROM OutwardItemTransaction o WHERE o.itemType = :itemType " +
           "AND o.createdAt BETWEEN :from AND :to ORDER BY o.createdAt DESC")
    Page<OutwardItemTransaction> findByItemTypeAndDateRange(@Param("itemType") OutwardItemTransaction.ItemType itemType,
                                                             @Param("from") LocalDateTime from,
                                                             @Param("to") LocalDateTime to,
                                                             Pageable pageable);

    @Query("SELECT o.itemType, COUNT(o), SUM(o.quantity) FROM OutwardItemTransaction o " +
           "WHERE o.createdAt BETWEEN :from AND :to GROUP BY o.itemType")
    List<Object[]> getIssueSummaryByType(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT o.issuedTo, COUNT(o), SUM(o.quantity) FROM OutwardItemTransaction o " +
           "WHERE o.itemType = :itemType AND o.createdAt BETWEEN :from AND :to " +
           "GROUP BY o.issuedTo ORDER BY COUNT(o) DESC")
    List<Object[]> getIssuesByRecipient(@Param("itemType") OutwardItemTransaction.ItemType itemType,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);
}
