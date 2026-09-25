package com.nector.userservice.repository;

import com.nector.userservice.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByOrderId(Long orderId);

    Optional<Invoice> findByOrderConfirmationId(Long orderConfirmationId);

    List<Invoice> findByDistributorIdOrderByCreatedAtDesc(Long distributorId);

    List<Invoice> findByInvoiceStatusOrderByCreatedAtDesc(Invoice.InvoiceStatus status);

    boolean existsByOrderId(Long orderId);

    boolean existsByInvoiceNumber(String invoiceNumber);

    // --- Report queries ---

    @Query("SELECT i FROM Invoice i WHERE (:distributorId IS NULL OR i.distributorId = :distributorId) " +
           "AND i.invoiceDate BETWEEN :from AND :to ORDER BY i.invoiceDate DESC")
    Page<Invoice> findByDistributorAndDateRange(@Param("distributorId") Long distributorId,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to,
                                                 Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i " +
           "WHERE (:distributorId IS NULL OR i.distributorId = :distributorId) " +
           "AND i.invoiceDate BETWEEN :from AND :to")
    BigDecimal sumGrandTotalByDistributorAndDateRange(@Param("distributorId") Long distributorId,
                                                      @Param("from") LocalDateTime from,
                                                      @Param("to") LocalDateTime to);

    @Query("SELECT i.distributorId, i.distributorName, COUNT(i), SUM(i.grandTotal) " +
           "FROM Invoice i WHERE i.invoiceDate BETWEEN :from AND :to " +
           "GROUP BY i.distributorId, i.distributorName ORDER BY SUM(i.grandTotal) DESC")
    List<Object[]> getSalesByDistributor(@Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to,
                                         Pageable pageable);

    @Query(value = "SELECT EXTRACT(MONTH FROM i.invoice_date), EXTRACT(YEAR FROM i.invoice_date), " +
                   "SUM(i.grand_total), COUNT(i.id), COALESCE(SUM(ili.quantity), 0) " +
                   "FROM invoices i " +
                   "LEFT JOIN invoice_line_items ili ON ili.invoice_id = i.id " +
                   "WHERE (:distributorId IS NULL OR i.distributor_id = :distributorId) " +
                   "AND i.invoice_date BETWEEN :from AND :to " +
                   "GROUP BY EXTRACT(YEAR FROM i.invoice_date), EXTRACT(MONTH FROM i.invoice_date) " +
                   "ORDER BY EXTRACT(YEAR FROM i.invoice_date), EXTRACT(MONTH FROM i.invoice_date)",
           nativeQuery = true)
    List<Object[]> getMonthlySalesTrend(@Param("distributorId") Long distributorId,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);
}
