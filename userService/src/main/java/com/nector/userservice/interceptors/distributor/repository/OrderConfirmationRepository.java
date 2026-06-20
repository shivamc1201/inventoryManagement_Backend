package com.nector.userservice.interceptors.distributor.repository;

import com.nector.userservice.interceptors.distributor.model.OrderConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderConfirmationRepository extends JpaRepository<OrderConfirmation, Long> {
    Optional<OrderConfirmation> findByOrderId(Long orderId);
    List<OrderConfirmation> findByDistributorIdOrderByConfirmedAtDesc(Long distributorId);
    boolean existsByOrderId(Long orderId);

    @Query("SELECT oc FROM OrderConfirmation oc LEFT JOIN FETCH oc.itemConfirmations WHERE oc.distributorId = :distributorId ORDER BY oc.confirmedAt DESC")
    List<OrderConfirmation> findByDistributorIdWithItems(@Param("distributorId") Long distributorId);

    List<OrderConfirmation> findByApprovalStatusOrderByConfirmedAtDesc(OrderConfirmation.ApprovalStatus approvalStatus);

    @Query("SELECT oc FROM OrderConfirmation oc LEFT JOIN FETCH oc.itemConfirmations WHERE oc.id = :id")
    Optional<OrderConfirmation> findByIdWithItems(@Param("id") Long id);
}