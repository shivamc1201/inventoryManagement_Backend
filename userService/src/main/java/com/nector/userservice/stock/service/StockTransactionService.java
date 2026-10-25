package com.nector.userservice.stock.service;

import com.nector.userservice.stock.entity.StockTransaction;
import com.nector.userservice.stock.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockTransactionService {

    private final StockTransactionRepository repository;

    public void record(StockTransaction.ItemType itemType,
                       Long itemId,
                       String itemCode,
                       String itemName,
                       StockTransaction.TxnType txnType,
                       BigDecimal quantity,
                       String unit,
                       String partyName,
                       String voucherNo,
                       String referenceNo,
                       LocalDate txnDate) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Skipping stock transaction record: zero/null quantity for item {} {}", itemType, itemId);
            return;
        }

        StockTransaction txn = StockTransaction.builder()
                .itemType(itemType)
                .itemId(itemId)
                .itemCode(itemCode)
                .itemName(itemName)
                .txnType(txnType)
                .quantity(quantity)
                .unit(unit)
                .partyName(partyName)
                .voucherNo(voucherNo)
                .referenceNo(referenceNo)
                .txnDate(txnDate != null ? txnDate : LocalDate.now())
                .build();

        repository.save(txn);
        log.info("StockTransaction recorded: {} {} {} qty={}", txnType, itemType, itemName, quantity);
    }
}
