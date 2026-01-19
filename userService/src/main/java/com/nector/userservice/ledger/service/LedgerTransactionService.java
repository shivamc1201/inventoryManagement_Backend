package com.nector.userservice.ledger.service;

import com.nector.userservice.ledger.dto.CreateTransactionRequest;
import com.nector.userservice.ledger.dto.TransactionResponse;
import com.nector.userservice.ledger.entity.LedgerAccount;
import com.nector.userservice.ledger.entity.LedgerAuditLog;
import com.nector.userservice.ledger.entity.LedgerTransaction;
import com.nector.userservice.ledger.entity.LedgerTransactionCategory;
import com.nector.userservice.ledger.enums.TransactionType;
import com.nector.userservice.ledger.repository.LedgerAuditLogRepository;
import com.nector.userservice.ledger.repository.LedgerTransactionCategoryRepository;
import com.nector.userservice.ledger.repository.LedgerTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for managing ledger transactions
 * Handles transaction creation, validation, and queries
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LedgerTransactionService {

    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final LedgerTransactionCategoryRepository categoryRepository;
    private final LedgerAccountService ledgerAccountService;
    private final LedgerCalculationService calculationService;
    private final LedgerAuditLogRepository auditLogRepository;

    private final AtomicLong referenceCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    /**
     * Create a new ledger transaction
     */
    public TransactionResponse createTransaction(Long accountId, CreateTransactionRequest request, String createdBy) {
        log.info("Creating transaction for account: {}, type: {}", accountId, request.getTransactionType());

        // Validate transaction rules
        validateTransactionRules(accountId, request);

        // Get account with lock to prevent concurrent balance updates
        LedgerAccount account = ledgerAccountService.getLedgerAccountWithLock(accountId);

        // Create transaction
        LedgerTransaction transaction = new LedgerTransaction(
                account,
                request.getTransactionDate(),
                request.getTransactionType(),
                generateReferenceNumber(request),
                request.getDescription(),
                createdBy
        );

        // Set amount based on transaction type
        setTransactionAmount(transaction, request);

        // Set category if provided
        if (request.getCategoryId() != null) {
            LedgerTransactionCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
            transaction.setCategory(category);
        }

        // Set optional fields
        transaction.setNotes(request.getNotes());
        transaction.setExternalReference(request.getExternalReference());

        // Calculate running balance
        BigDecimal runningBalance = calculationService.calculateRunningBalance(
                account, transaction.getDebitAmount(), transaction.getCreditAmount());
        transaction.setRunningBalance(runningBalance);

        // Save transaction (trigger will update account balance)
        LedgerTransaction savedTransaction = ledgerTransactionRepository.save(transaction);
        
        // Save audit log
        LedgerAuditLog auditLog = new LedgerAuditLog(account, savedTransaction, "TRANSACTION_CREATED", createdBy);
        auditLogRepository.save(auditLog);


        log.info("Created transaction with ID: {} for account: {}", savedTransaction.getId(), accountId);

        return mapToTransactionResponse(savedTransaction);
    }

    /**
     * Create opening balance transaction
     */
    public TransactionResponse createOpeningBalance(Long accountId, BigDecimal amount, 
                                                  String description, String createdBy) {
        // Check if opening balance already exists
        if (ledgerTransactionRepository.existsByLedgerAccountIdAndTransactionType(
                accountId, TransactionType.OPENING_BALANCE)) {
            throw new IllegalArgumentException("Opening balance already exists for this account");
        }

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionDate(LocalDate.now());
        request.setTransactionType(TransactionType.OPENING_BALANCE);
        request.setDescription(description);
        request.setAmount(amount);

        return createTransaction(accountId, request, createdBy);
    }

    /**
     * Reverse a transaction
     */
    public TransactionResponse reverseTransaction(Long transactionId, String reason, String createdBy) {
        log.info("Reversing transaction: {}", transactionId);

        LedgerTransaction originalTransaction = ledgerTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (originalTransaction.getIsReversal()) {
            throw new IllegalArgumentException("Cannot reverse a reversal transaction");
        }

        // Create reversal transaction
        LedgerTransaction reversalTransaction = new LedgerTransaction(
                originalTransaction.getLedgerAccount(),
                LocalDate.now(),
                originalTransaction.getTransactionType(),
                generateReversalReferenceNumber(originalTransaction.getReferenceNumber()),
                "REVERSAL: " + reason,
                createdBy
        );

        // Reverse the amounts
        reversalTransaction.setDebitAmount(originalTransaction.getCreditAmount());
        reversalTransaction.setCreditAmount(originalTransaction.getDebitAmount());
        reversalTransaction.setIsReversal(true);
        reversalTransaction.setReversedTransaction(originalTransaction);
        reversalTransaction.setCategory(originalTransaction.getCategory());

        // Calculate running balance
        BigDecimal runningBalance = calculationService.calculateRunningBalance(
                originalTransaction.getLedgerAccount(),
                reversalTransaction.getDebitAmount(),
                reversalTransaction.getCreditAmount()
        );
        reversalTransaction.setRunningBalance(runningBalance);

        LedgerTransaction savedReversal = ledgerTransactionRepository.save(reversalTransaction);
        
        // Save audit log
        LedgerAuditLog auditLog = new LedgerAuditLog(originalTransaction.getLedgerAccount(), savedReversal, "TRANSACTION_REVERSED", createdBy);
        auditLogRepository.save(auditLog);
        log.info("Created reversal transaction with ID: {}", savedReversal.getId());

        return mapToTransactionResponse(savedReversal);
    }

    /**
     * Get transactions for an account with pagination
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(Long accountId, Pageable pageable) {
        Page<LedgerTransaction> transactions = ledgerTransactionRepository
                .findByLedgerAccountIdOrderByCreatedAtDesc(accountId, pageable);
        
        return transactions.map(this::mapToTransactionResponse);
    }

    /**
     * Get transactions with filters
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsWithFilters(Long accountId, LocalDate startDate, 
                                                              LocalDate endDate, TransactionType transactionType,
                                                              String searchTerm, Pageable pageable) {
        Page<LedgerTransaction> transactions;

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            transactions = ledgerTransactionRepository.searchTransactions(accountId, searchTerm, pageable);
        } else if (startDate != null && endDate != null) {
            transactions = ledgerTransactionRepository.findByAccountAndDateRange(
                    accountId, startDate, endDate, pageable);
        } else if (transactionType != null) {
            transactions = ledgerTransactionRepository.findByLedgerAccountIdAndTransactionTypeOrderByCreatedAtDesc(
                    accountId, transactionType, pageable);
        } else {
            transactions = ledgerTransactionRepository.findByLedgerAccountIdOrderByCreatedAtDesc(
                    accountId, pageable);
        }

        return transactions.map(this::mapToTransactionResponse);
    }

    /**
     * Get transactions for export
     */
    @Transactional(readOnly = true)
    public List<LedgerTransaction> getTransactionsForExport(Long accountId, LocalDate startDate, 
                                                          LocalDate endDate, TransactionType transactionType) {
        return ledgerTransactionRepository.findTransactionsForExport(
                accountId, startDate, endDate, transactionType);
    }

    // Private helper methods

    private void validateTransactionRules(Long accountId, CreateTransactionRequest request) {
        // Validate opening balance rule
        if (request.getTransactionType() == TransactionType.OPENING_BALANCE) {
            if (ledgerTransactionRepository.existsByLedgerAccountIdAndTransactionType(
                    accountId, TransactionType.OPENING_BALANCE)) {
                throw new IllegalArgumentException("Opening balance already exists for this account");
            }
        }

        // Validate adjustment transaction
        if (request.getTransactionType() == TransactionType.ADJUSTMENT) {
            if (request.getIsDebit() == null) {
                throw new IllegalArgumentException("Adjustment transactions must specify debit or credit");
            }
        }

        // Validate transaction date
        if (request.getTransactionDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Transaction date cannot be in the future");
        }
    }

    private void setTransactionAmount(LedgerTransaction transaction, CreateTransactionRequest request) {
        TransactionType type = request.getTransactionType();
        BigDecimal amount = request.getAmount();

        if (type.isAdjustment()) {
            // For adjustments, use the isDebit flag
            if (Boolean.TRUE.equals(request.getIsDebit())) {
                transaction.setAsDebit(amount);
            } else {
                transaction.setAsCredit(amount);
            }
        } else if (Boolean.TRUE.equals(type.isDebit())) {
            transaction.setAsDebit(amount);
        } else if (Boolean.TRUE.equals(type.isCredit())) {
            transaction.setAsCredit(amount);
        } else {
            throw new IllegalArgumentException("Invalid transaction type configuration");
        }
    }

    private String generateReferenceNumber(CreateTransactionRequest request) {
        if (request.getReferenceNumber() != null && !request.getReferenceNumber().trim().isEmpty()) {
            return request.getReferenceNumber().trim();
        }

        // Auto-generate reference number
        String dateStr = request.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String typePrefix = request.getTransactionType().name().substring(0, 3);
        long counter = referenceCounter.incrementAndGet();
        
        return String.format("%s-%s-%06d", typePrefix, dateStr, counter);
    }

    private String generateReversalReferenceNumber(String originalReference) {
        return "REV-" + originalReference;
    }

    private TransactionResponse mapToTransactionResponse(LedgerTransaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setTransactionType(transaction.getTransactionType());
        response.setReferenceNumber(transaction.getReferenceNumber());
        response.setDescription(transaction.getDescription());
        response.setDebitAmount(transaction.getDebitAmount());
        response.setCreditAmount(transaction.getCreditAmount());
        response.setRunningBalance(transaction.getRunningBalance());
        response.setCategoryName(transaction.getCategory() != null ? 
                transaction.getCategory().getCategoryName() : null);
        response.setNotes(transaction.getNotes());
        response.setExternalReference(transaction.getExternalReference());
        response.setIsReversal(transaction.getIsReversal());
        response.setReversedTransactionId(transaction.getReversedTransaction() != null ? 
                transaction.getReversedTransaction().getId() : null);
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCreatedBy(transaction.getCreatedBy());
        
        return response;
    }
}