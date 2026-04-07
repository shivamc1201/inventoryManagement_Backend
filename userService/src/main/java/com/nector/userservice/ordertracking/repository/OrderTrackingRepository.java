package com.nector.userservice.ordertracking.repository;

import com.nector.userservice.ordertracking.entity.OrderTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTrackingRepository extends JpaRepository<OrderTracking, Long> {

    boolean existsByOrderNumber(String orderNumber);
    
    OrderTracking findByOrderNumber(String orderNumber);

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

    // Methods for distributor-based filtering
    Page<OrderTracking> findByDistributorId(Long distributorId, Pageable pageable);
    
    List<OrderTracking> findByDistributorId(Long distributorId);
    
    @Query("SELECT COUNT(o) FROM OrderTracking o WHERE o.distributorId = :distributorId")
    long countByDistributorId(@Param("distributorId") Long distributorId);
}
