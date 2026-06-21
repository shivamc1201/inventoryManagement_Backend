package com.nector.userservice.dispatch.repository;

import com.nector.userservice.dispatch.entity.InventoryVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryVerificationRepository extends JpaRepository<InventoryVerification, Long> {
    Optional<InventoryVerification> findByOrderId(Long orderId);
}