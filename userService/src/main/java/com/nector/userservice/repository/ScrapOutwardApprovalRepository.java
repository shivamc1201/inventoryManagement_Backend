package com.nector.userservice.repository;

import com.nector.userservice.model.ScrapOutwardApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScrapOutwardApprovalRepository extends JpaRepository<ScrapOutwardApproval, Long> {

    List<ScrapOutwardApproval> findByApprovalStatus(String approvalStatus);

    // --- Report queries ---

    @Query("SELECT s.approvalStatus, COUNT(s), COALESCE(SUM(s.quantity * s.quotedSellingPrice), 0) " +
           "FROM ScrapOutwardApproval s WHERE s.requestedOn BETWEEN :from AND :to " +
           "GROUP BY s.approvalStatus")
    List<Object[]> getDisposalSummaryByStatus(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(s.quantity * s.quotedSellingPrice), 0) FROM ScrapOutwardApproval s " +
           "WHERE s.approvalStatus = 'APPROVED' AND s.reviewedOn BETWEEN :from AND :to")
    BigDecimal getTotalScrapRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<ScrapOutwardApproval> findByRequestedOnBetweenOrderByRequestedOnDesc(LocalDateTime from, LocalDateTime to);
}
