package com.nector.userservice.ordertracking.repository;

import com.nector.userservice.ordertracking.entity.OrderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderDocumentRepository extends JpaRepository<OrderDocument, Long> {

    Optional<OrderDocument> findByOrderIdAndDocType(Long orderId, String docType);
    
    boolean existsByOrderIdAndDocType(Long orderId, String docType);
}
