package com.nector.userservice.repository;

import com.nector.userservice.model.OrderWithSalesPerson;
import com.nector.userservice.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderWithSalesPerson, Long> {

    List<OrderWithSalesPerson> findByStatus(OrderStatus status);

    List<OrderWithSalesPerson> findBySalespersonId(Long salespersonId);

    List<OrderWithSalesPerson> findByDistributorId(Long distributorId);

    List<OrderWithSalesPerson> findByCreatedAtBetween(LocalDateTime dateFrom, LocalDateTime dateTo);

    List<OrderWithSalesPerson> findBySalespersonIdAndCreatedAtBetween(Long salespersonId, LocalDateTime dateFrom, LocalDateTime dateTo);

    List<OrderWithSalesPerson> findByDistributorIdAndCreatedAtBetween(Long distributorId, LocalDateTime dateFrom, LocalDateTime dateTo);

    // Volume analytics methods - only count GDN_GENERATED orders
    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE o.status = com.nector.userservice.enums.OrderStatus.GDN_GENERATED AND o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countGdnOrdersBetweenDates(@Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE o.status = com.nector.userservice.enums.OrderStatus.GDN_GENERATED AND o.salespersonId = :salespersonId AND o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countGdnOrdersBySalespersonBetweenDates(
            @Param("salespersonId") Long salespersonId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE o.status = com.nector.userservice.enums.OrderStatus.GDN_GENERATED AND o.distributorId = :distributorId AND o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countGdnOrdersByDistributorBetweenDates(
            @Param("distributorId") Long distributorId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT o FROM OrderWithSalesPerson o WHERE o.status = com.nector.userservice.enums.OrderStatus.GDN_GENERATED AND o.createdAt BETWEEN :dateFrom AND :dateTo")
    List<OrderWithSalesPerson> findGdnOrdersByCreatedAtBetween(@Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);

    @Query("SELECT o FROM OrderWithSalesPerson o WHERE o.status = com.nector.userservice.enums.OrderStatus.GDN_GENERATED AND o.salespersonId = :salespersonId AND o.createdAt BETWEEN :dateFrom AND :dateTo")
    List<OrderWithSalesPerson> findGdnOrdersBySalespersonAndCreatedAtBetween(
            @Param("salespersonId") Long salespersonId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT o FROM OrderWithSalesPerson o WHERE o.status = com.nector.userservice.enums.OrderStatus.GDN_GENERATED AND o.distributorId = :distributorId AND o.createdAt BETWEEN :dateFrom AND :dateTo")
    List<OrderWithSalesPerson> findGdnOrdersByDistributorAndCreatedAtBetween(
            @Param("distributorId") Long distributorId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE o.status = com.nector.userservice.enums.OrderStatus.GDN_GENERATED AND o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal sumGdnAmountBetweenDates(@Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE o.status = com.nector.userservice.enums.OrderStatus.GDN_GENERATED AND o.salespersonId = :salespersonId AND o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal sumGdnAmountBySalespersonBetweenDates(
            @Param("salespersonId") Long salespersonId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE o.status = com.nector.userservice.enums.OrderStatus.GDN_GENERATED AND o.distributorId = :distributorId AND o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal sumGdnAmountByDistributorBetweenDates(
            @Param("distributorId") Long distributorId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT o FROM OrderWithSalesPerson o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:salespersonId IS NULL OR o.salespersonId = :salespersonId) AND " +
           "(:distributorId IS NULL OR o.distributorId = :distributorId) AND " +
           "(:dateFrom IS NULL OR o.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR o.createdAt <= :dateTo)")
    List<OrderWithSalesPerson> findWithFilters(
            @Param("status") OrderStatus status,
            @Param("salespersonId") Long salespersonId,
            @Param("distributorId") Long distributorId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
           "(:status IS NULL OR o.status = :status)")
    long countWithFilters(
            @Param("status") OrderStatus status
    );

    @Query("SELECT o.status, COUNT(o) FROM OrderWithSalesPerson o GROUP BY o.status")
    List<Object[]> getSummaryByStatus();

    @Query("SELECT o FROM OrderWithSalesPerson o WHERE " +
           "o.salespersonId IN :salespersonIds AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:dateFrom IS NULL OR o.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR o.createdAt <= :dateTo)")
    List<OrderWithSalesPerson> findBySalespersonIds(
            @Param("salespersonIds") List<Long> salespersonIds,
            @Param("status") OrderStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
           "o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countOrdersBetweenDates(@Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
           "o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal getTotalAmountBetweenDates(@Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo);

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
            "o.salespersonId = :salespersonId AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countOrdersBySalespersonBetweenDates(
            @Param("salespersonId") Long salespersonId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
            "o.salespersonId = :salespersonId AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal getTotalAmountBySalespersonBetweenDates(
            @Param("salespersonId") Long salespersonId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
            "o.distributorId = :distributorId AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countOrdersByDistributorBetweenDates(
            @Param("distributorId") Long distributorId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
            "o.distributorId = :distributorId AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal getTotalAmountByDistributorBetweenDates(
            @Param("distributorId") Long distributorId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
            "o.distributorId = :distributorId")
    Long countOrdersByDistributor(@Param("distributorId") Long distributorId);

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
            "o.distributorId = :distributorId")
    BigDecimal getTotalAmountByDistributor(@Param("distributorId") Long distributorId);

    @Query(value = "SELECT EXTRACT(MONTH FROM created_at) as month, EXTRACT(YEAR FROM created_at) as year, " +
            "COALESCE(SUM(total_cart_amount), 0) as revenue, COUNT(*) as order_count " +
            "FROM carts WHERE status = 'GDN_GENERATED' AND created_at BETWEEN :dateFrom AND :dateTo " +
            "GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at) ORDER BY year, month",
            nativeQuery = true)
    List<Object[]> getMonthlyGdnRevenue(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
            "o.status = :status AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countOrdersByStatusBetweenDates(
            @Param("status") OrderStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
            "o.status = :status AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal getTotalAmountByStatusBetweenDates(
            @Param("status") OrderStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
            "o.status = :status AND " +
            "o.distributorId = :distributorId")
    Long countOrdersByDistributorAndStatus(
            @Param("distributorId") Long distributorId,
            @Param("status") OrderStatus status
    );

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
            "o.status = :status AND " +
            "o.distributorId = :distributorId")
    BigDecimal getTotalAmountByDistributorAndStatus(
            @Param("distributorId") Long distributorId,
            @Param("status") OrderStatus status
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
            "o.salespersonId = :salespersonId AND " +
            "o.status = :status AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countOrdersBySalespersonAndStatusBetweenDates(
            @Param("salespersonId") Long salespersonId,
            @Param("status") OrderStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
            "o.salespersonId = :salespersonId AND " +
            "o.status = :status AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal getTotalAmountBySalespersonAndStatusBetweenDates(
            @Param("salespersonId") Long salespersonId,
            @Param("status") OrderStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

}
