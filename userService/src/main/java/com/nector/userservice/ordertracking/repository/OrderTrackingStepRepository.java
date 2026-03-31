package com.nector.userservice.ordertracking.repository;

import com.nector.userservice.ordertracking.entity.OrderTrackingStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderTrackingStepRepository extends JpaRepository<OrderTrackingStep, Long> {

    List<OrderTrackingStep> findByOrderIdOrderByStepSequenceAsc(Long orderId);

    Optional<OrderTrackingStep> findByIdAndOrderId(Long id, Long orderId);

    Optional<OrderTrackingStep> findByOrderIdAndStepSequence(Long orderId, int stepSequence);
}
