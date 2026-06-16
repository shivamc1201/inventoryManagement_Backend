package com.nector.userservice.transaction.repository;

import com.nector.userservice.transaction.entity.TransactionLedger;
import com.nector.userservice.transaction.enums.LedgerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionLedgerRepository extends JpaRepository<TransactionLedger, Long> {

    boolean existsByLedgerNameIgnoreCase(String ledgerName);

    Optional<TransactionLedger> findByLedgerNameIgnoreCase(String ledgerName);

    List<TransactionLedger> findByLedgerType(LedgerType ledgerType);
}
