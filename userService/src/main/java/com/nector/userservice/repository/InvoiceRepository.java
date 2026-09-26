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

    @Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i " +
           "WHERE i.distributorId IN :distributorIds " +
           "AND i.invoiceDate BETWEEN :from AND :to")
    BigDecimal sumGrandTotalForDistributors(@Param("distributorIds") List<Long> distributorIds,
                                            @Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    @Query(value = "SELECT CEIL(EXTRACT(DAY FROM i.invoice_date) / 7.0) AS week_num, SUM(i.grand_total) AS total " +
                   "FROM invoices i " +
                   "WHERE i.distributor_id IN :distributorIds " +
                   "AND i.invoice_date BETWEEN :from AND :to " +
                   "GROUP BY week_num ORDER BY week_num",
           nativeQuery = true)
    List<Object[]> getWeeklySalesForDistributors(@Param("distributorIds") List<Long> distributorIds,
                                                  @Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);

    @Query("SELECT i.distributorId, i.distributorName, COUNT(i), SUM(i.grandTotal) " +
           "FROM Invoice i WHERE i.invoiceDate BETWEEN :from AND :to " +
           "GROUP BY i.distributorId, i.distributorName ORDER BY SUM(i.grandTotal) DESC")
    List<Object[]> getSalesByDistributor(@Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to,
                                         Pageable pageable);

    @Query(value = "SELECT CEIL(EXTRACT(DAY FROM i.invoice_date) / 7.0) AS week_num, " +
                   "SUM(i.grand_total) AS total " +
                   "FROM invoices i " +
                   "WHERE i.invoice_date BETWEEN :from AND :to " +
                   "AND (:distributorId IS NULL OR i.distributor_id = :distributorId) " +
                   "GROUP BY CEIL(EXTRACT(DAY FROM i.invoice_date) / 7.0) " +
                   "ORDER BY week_num",
           nativeQuery = true)
    List<Object[]> getWeeklySalesSummary(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          @Param("distributorId") Long distributorId);

    @Query(value = "SELECT sp.region, SUM(i.grand_total), COUNT(i.id), COUNT(DISTINCT i.distributor_id) " +
                   "FROM invoices i " +
                   "JOIN distributors d ON d.id = i.distributor_id " +
                   "JOIN sales_persons sp ON sp.id = d.salesperson_id " +
                   "WHERE i.invoice_date BETWEEN :from AND :to " +
                   "AND sp.region IS NOT NULL " +
                   "GROUP BY sp.region ORDER BY SUM(i.grand_total) DESC",
           nativeQuery = true)
    List<Object[]> getSalesByRegion(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

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
