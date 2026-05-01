package com.nector.userservice.repository;

import com.nector.userservice.model.OutwardItemTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutwardItemTransactionRepository extends JpaRepository<OutwardItemTransaction, Long> {

    List<OutwardItemTransaction> findByItemType(OutwardItemTransaction.ItemType itemType);

    List<OutwardItemTransaction> findByTransactionType(OutwardItemTransaction.TransactionType transactionType);

    List<OutwardItemTransaction> findByMaterialCode(String materialCode);

    List<OutwardItemTransaction> findByItemTypeAndTransactionType(
            OutwardItemTransaction.ItemType itemType,
            OutwardItemTransaction.TransactionType transactionType);
}
