package com.nector.userservice.ledger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nector.userservice.ledger.entity.LedgerAccount;
import com.nector.userservice.ledger.entity.LedgerAuditLog;
import com.nector.userservice.ledger.entity.LedgerTransaction;
import com.nector.userservice.ledger.repository.LedgerAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerAuditService {

    private final LedgerAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void logTransactionCreated(LedgerAccount account, LedgerTransaction transaction, String userId) {
        try {
            LedgerAuditLog auditLog = new LedgerAuditLog(account, transaction, "TRANSACTION_CREATED", userId);

            // Store transaction details as JSON
            String transactionJson = objectMapper.writeValueAsString(Map.of(
                    "transactionId", transaction.getId(),
                    "transactionType", transaction.getTransactionType(),
                    "amount", transaction.getTransactionAmount(),
                    "description", transaction.getDescription(),
                    "referenceNumber", transaction.getReferenceNumber(),
                    "runningBalance", transaction.getRunningBalance()
            ));

            auditLog.setNewValues(transactionJson);
            auditLogRepository.save(auditLog);

            log.info("Audit log created for transaction: {}", transaction.getId());
        } catch (Exception e) {
            log.error("Failed to create audit log for transaction: {}", transaction.getId(), e);
        }
    }
}

