package com.nector.userservice.repository;

import com.nector.userservice.model.ProformaInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProformaInvoiceRepository extends JpaRepository<ProformaInvoice, Long> {
    Optional<ProformaInvoice> findByCartId(Long cartId);
}