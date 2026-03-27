package com.nector.userservice.repository;

import com.nector.userservice.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByOrderId(Long orderId);
    
    Optional<Invoice> findByOrderConfirmationId(Long orderConfirmationId);
    
    List<Invoice> findByDistributorIdOrderByCreatedAtDesc(Long distributorId);
    
    List<Invoice> findByInvoiceStatusOrderByCreatedAtDesc(Invoice.InvoiceStatus status);
    
    boolean existsByOrderId(Long orderId);
    
    boolean existsByInvoiceNumber(String invoiceNumber);
}
