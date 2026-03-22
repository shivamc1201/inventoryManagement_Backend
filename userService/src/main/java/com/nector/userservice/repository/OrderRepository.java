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
}
