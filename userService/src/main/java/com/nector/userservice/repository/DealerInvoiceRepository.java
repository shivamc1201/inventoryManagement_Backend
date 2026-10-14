package com.nector.userservice.repository;

import com.nector.userservice.model.DealerInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DealerInvoiceRepository extends JpaRepository<DealerInvoice, Long> {
    Optional<DealerInvoice> findByDealerOrderId(Long dealerOrderId);
    List<DealerInvoice> findByDealerIdOrderByCreatedAtDesc(Long dealerId);
    List<DealerInvoice> findByDistributorIdOrderByCreatedAtDesc(Long distributorId);

    // [dealerId, dealerName, distributorId, distributorName, orderCount, totalQty, totalRevenue]
    @Query(value =
        "SELECT di.dealer_id, di.dealer_name, di.distributor_id, di.distributor_name, " +
        "COUNT(di.id) AS order_count, COALESCE(SUM(do2.quantity), 0) AS total_qty, COALESCE(SUM(di.grand_total), 0) AS total_revenue " +
        "FROM dealer_invoices di " +
        "LEFT JOIN dealer_orders do2 ON di.dealer_order_id = do2.id " +
        "WHERE di.created_at BETWEEN :from AND :to " +
        "AND (:distributorId IS NULL OR di.distributor_id = :distributorId) " +
        "GROUP BY di.dealer_id, di.dealer_name, di.distributor_id, di.distributor_name " +
        "ORDER BY total_revenue DESC",
        nativeQuery = true)
    List<Object[]> getDealerWiseSummary(@Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to,
                                        @Param("distributorId") Long distributorId);
}
