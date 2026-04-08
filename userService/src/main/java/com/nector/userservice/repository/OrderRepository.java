package com.nector.userservice.repository;

import com.nector.userservice.model.OrderWithSalesPerson;
import com.nector.userservice.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderWithSalesPerson, Long> {

    List<OrderWithSalesPerson> findByStatus(OrderStatus status);

    List<OrderWithSalesPerson> findBySalespersonId(Long salespersonId);

    List<OrderWithSalesPerson> findByDistributorId(Long distributorId);

    List<OrderWithSalesPerson> findByCreatedAtBetween(LocalDate dateFrom, LocalDate dateTo);

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
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
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
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
           "o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countOrdersBetweenDates(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
           "o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal getTotalAmountBetweenDates(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
            "o.salespersonId = :salespersonId AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countOrdersBySalespersonBetweenDates(
            @Param("salespersonId") Long salespersonId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
            "o.salespersonId = :salespersonId AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal getTotalAmountBySalespersonBetweenDates(
            @Param("salespersonId") Long salespersonId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
            "o.distributorId = :distributorId AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    Long countOrdersByDistributorBetweenDates(
            @Param("distributorId") Long distributorId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
            "o.distributorId = :distributorId AND " +
            "o.createdAt BETWEEN :dateFrom AND :dateTo")
    BigDecimal getTotalAmountByDistributorBetweenDates(
            @Param("distributorId") Long distributorId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("SELECT COUNT(o) FROM OrderWithSalesPerson o WHERE " +
            "o.distributorId = :distributorId")
    Long countOrdersByDistributor(@Param("distributorId") Long distributorId);

    @Query("SELECT COALESCE(SUM(o.totalCartAmount), 0) FROM OrderWithSalesPerson o WHERE " +
            "o.distributorId = :distributorId")
    BigDecimal getTotalAmountByDistributor(@Param("distributorId") Long distributorId);

}
