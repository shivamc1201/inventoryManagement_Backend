package com.nector.userservice.repository;

import com.nector.userservice.model.DealerInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DealerInvoiceRepository extends JpaRepository<DealerInvoice, Long> {
    Optional<DealerInvoice> findByDealerOrderId(Long dealerOrderId);
    List<DealerInvoice> findByDealerIdOrderByCreatedAtDesc(Long dealerId);
    List<DealerInvoice> findByDistributorIdOrderByCreatedAtDesc(Long distributorId);
}
