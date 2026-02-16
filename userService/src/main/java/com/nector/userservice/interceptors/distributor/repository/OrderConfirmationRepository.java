package com.nector.userservice.interceptors.distributor.repository;

import com.nector.userservice.interceptors.distributor.model.OrderConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderConfirmationRepository extends JpaRepository<OrderConfirmation, Long> {
    Optional<OrderConfirmation> findByOrderId(Long orderId);
    List<OrderConfirmation> findByDistributorIdOrderByConfirmedAtDesc(Long distributorId);
    boolean existsByOrderId(Long orderId);
}