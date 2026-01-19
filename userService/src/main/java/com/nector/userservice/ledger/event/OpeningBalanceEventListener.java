package com.nector.userservice.ledger.event;

import com.nector.userservice.ledger.dto.CreateTransactionRequest;
import com.nector.userservice.ledger.enums.TransactionType;
import com.nector.userservice.ledger.service.LedgerTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpeningBalanceEventListener {

    private final LedgerTransactionService ledgerTransactionService;

    @EventListener
    public void handleOpeningBalanceEvent(OpeningBalanceEvent event) {
        log.info("Processing opening balance event for account: {}", event.getAccountId());
        
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionDate(LocalDate.now());
        request.setTransactionType(TransactionType.OPENING_BALANCE);
        request.setDescription(event.getDescription());
        request.setAmount(event.getAmount());

        ledgerTransactionService.createTransaction(event.getAccountId(), request, event.getCreatedBy());
    }
}