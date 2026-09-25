package com.nector.userservice.ordertracking.repository;

import com.nector.userservice.ordertracking.entity.OrderTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderTrackingRepository extends JpaRepository<OrderTracking, Long> {

    boolean existsByOrderNumber(String orderNumber);
    
    OrderTracking findByOrderNumber(String orderNumber);
    
    OrderTracking findByCartId(Long cartId);

    /**
     * Supports the 3 filter values from the frontend's filter chips:
     * status = 'pending'   → any step is pending or in-progress
     * status = 'completed' → all 11 steps are completed
     * status = 'cancelled' → any step is cancelled
     * status = 'all' or null → no filter
     */
    @Query("SELECT DISTINCT o FROM OrderTracking o " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           "o.orderNumber LIKE %:search% OR o.distributorName LIKE %:search%) " +
           "AND (:status IS NULL OR :status = 'all' OR " +
           "(:status = 'completed' AND NOT EXISTS (" +
           "SELECT s FROM OrderTrackingStep s WHERE s.order = o " +
           "AND s.status <> com.nector.userservice.ordertracking.entity.StepStatus.COMPLETED)) OR " +
           "(:status = 'cancelled' AND EXISTS (" +
           "SELECT s FROM OrderTrackingStep s WHERE s.order = o " +
           "AND s.status = com.nector.userservice.ordertracking.entity.StepStatus.CANCELLED)) OR " +
           "(:status = 'pending' AND EXISTS (" +
           "SELECT s FROM OrderTrackingStep s WHERE s.order = o " +
           "AND s.status IN (com.nector.userservice.ordertracking.entity.StepStatus.PENDING, " +
           "com.nector.userservice.ordertracking.entity.StepStatus.IN_PROGRESS)))) " +
           "ORDER BY o.orderDate DESC")
    Page<OrderTracking> findFiltered(
        @Param("search") String search,
        @Param("status") String status,
        Pageable pageable
    );

    // For stats
    @Query("SELECT COUNT(o) FROM OrderTracking o WHERE NOT EXISTS " +
           "(SELECT s FROM OrderTrackingStep s WHERE s.order = o " +
           " AND s.status <> com.nector.userservice.ordertracking.entity.StepStatus.COMPLETED)")
    long countCompleted();

    @Query("SELECT COUNT(DISTINCT o) FROM OrderTracking o " +
           "JOIN OrderTrackingStep s ON s.order = o " +
           "WHERE s.status IN " +
           "(com.nector.userservice.ordertracking.entity.StepStatus.PENDING, " +
           " com.nector.userservice.ordertracking.entity.StepStatus.IN_PROGRESS)")
    long countPending();

    @Query("SELECT DISTINCT o FROM OrderTracking o " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           "o.orderNumber LIKE %:search% OR o.distributorName LIKE %:search%) " +
           "AND (:status IS NULL OR :status = 'all' OR " +
           "(:status = 'completed' AND NOT EXISTS (" +
           "SELECT s FROM OrderTrackingStep s WHERE s.order = o " +
           "AND s.status <> com.nector.userservice.ordertracking.entity.StepStatus.COMPLETED)) OR " +
           "(:status = 'cancelled' AND EXISTS (" +
           "SELECT s FROM OrderTrackingStep s WHERE s.order = o " +
           "AND s.status = com.nector.userservice.ordertracking.entity.StepStatus.CANCELLED)) OR " +
           "(:status = 'pending' AND EXISTS (" +
           "SELECT s FROM OrderTrackingStep s WHERE s.order = o " +
           "AND s.status IN (com.nector.userservice.ordertracking.entity.StepStatus.PENDING, " +
           "com.nector.userservice.ordertracking.entity.StepStatus.IN_PROGRESS)))) " +
           "AND o.salespersonId IN :salespersonIds " +
           "ORDER BY o.orderDate DESC")
    Page<OrderTracking> findFilteredBySalespersonIds(
        @Param("search") String search,
        @Param("status") String status,
        @Param("salespersonIds") Collection<Long> salespersonIds,
        Pageable pageable
    );

    @Modifying
    @Query(value = "UPDATE order_tracking ot " +
                   "SET salesperson_id = c.salesperson_id " +
                   "FROM carts c " +
                   "WHERE ot.cart_id = c.id " +
                   "AND ot.salesperson_id IS NULL " +
                   "AND c.salesperson_id IS NOT NULL",
           nativeQuery = true)
    int backfillSalespersonIds();

    // --- Salesman performance ---

    @Query(value = "SELECT ot.salesperson_id, MAX(c.salesperson_name) AS salesperson_name, " +
                   "COUNT(ot.id) AS order_count, SUM(ot.total_amount) AS total_value, " +
                   "SUM(COALESCE(g.total_weight, c.total_weight, 0)) / 1000 AS total_quantity_tons, " +
                   "SUM(COALESCE(g.total_weight, c.total_weight, 0)) AS total_quantity_kg " +
                   "FROM order_tracking ot " +
                   "LEFT JOIN carts c ON ot.cart_id = c.id " +
                   "LEFT JOIN gdn g ON g.order_id = ot.id " +
                   "WHERE ot.salesperson_id IS NOT NULL " +
                   "AND ot.order_date BETWEEN :from AND :to " +
                   "GROUP BY ot.salesperson_id " +
                   "ORDER BY SUM(ot.total_amount) DESC",
           nativeQuery = true)
    List<Object[]> getSalesmanPerformance(@Param("from") java.time.LocalDate from,
                                           @Param("to") java.time.LocalDate to);

    // Methods for distributor-based filtering
    Page<OrderTracking> findByDistributorId(Long distributorId, Pageable pageable);
    
    List<OrderTracking> findByDistributorId(Long distributorId);
    
    @Query("SELECT COUNT(o) FROM OrderTracking o WHERE o.distributorId = :distributorId")
    long countByDistributorId(@Param("distributorId") Long distributorId);

    // --- Report queries ---

    @Query("SELECT DISTINCT o FROM OrderTracking o WHERE o.distributorId = :distributorId " +
           "AND o.orderDate BETWEEN :from AND :to ORDER BY o.orderDate DESC")
    Page<OrderTracking> findByDistributorIdAndDateRange(@Param("distributorId") Long distributorId,
                                                         @Param("from") java.time.LocalDate from,
                                                         @Param("to") java.time.LocalDate to,
                                                         Pageable pageable);

    @Query("SELECT DISTINCT o FROM OrderTracking o " +
           "WHERE o.orderDate BETWEEN :from AND :to ORDER BY o.orderDate DESC")
    Page<OrderTracking> findAllByDateRange(@Param("from") java.time.LocalDate from,
                                            @Param("to") java.time.LocalDate to,
                                            Pageable pageable);
}
