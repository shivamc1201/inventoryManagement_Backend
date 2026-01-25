package com.nector.userservice.ledger.service;

import com.nector.userservice.ledger.dto.CreateLedgerAccountRequest;
import com.nector.userservice.ledger.dto.LedgerSummaryResponse;
import com.nector.userservice.ledger.entity.LedgerAccount;
import com.nector.userservice.ledger.enums.AccountStatus;
import com.nector.userservice.ledger.enums.TransactionType;
import com.nector.userservice.ledger.event.OpeningBalanceEvent;
import com.nector.userservice.ledger.repository.LedgerAccountRepository;
import com.nector.userservice.ledger.repository.LedgerTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing ledger accounts
 * Handles account creation, updates, and queries
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LedgerAccountService {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Create a new ledger account for a distributor
     */
    public LedgerAccount createLedgerAccount(CreateLedgerAccountRequest request, String createdBy) {
        log.info("Creating ledger account for company: {}, distributor: {}", 
                request.getCompanyId(), request.getDistributorId());

        // Check if account already exists
        if (ledgerAccountRepository.existsByCompanyIdAndDistributorId(
                request.getCompanyId(), request.getDistributorId())) {
            throw new IllegalArgumentException("Ledger account already exists for this distributor");
        }

        // Create the account
        LedgerAccount account = new LedgerAccount(
                request.getCompanyId(),
                request.getDistributorId(),
                request.getSalespersonId(),
                request.getAccountName(),
                createdBy
        );
        account.setCreditLimit(request.getCreditLimit());

        LedgerAccount savedAccount = ledgerAccountRepository.save(account);

        // Publish event for opening balance transaction if provided
        if (request.getOpeningBalance() != null && 
            request.getOpeningBalance().compareTo(BigDecimal.ZERO) > 0) {
            
            OpeningBalanceEvent event = new OpeningBalanceEvent(
                    savedAccount.getId(),
                    request.getOpeningBalance(),
                    request.getOpeningBalanceDescription() != null ? 
                            request.getOpeningBalanceDescription() : "Opening Balance",
                    createdBy
            );
            eventPublisher.publishEvent(event);
        }

        log.info("Created ledger account with ID: {} for distributor: {}", 
                savedAccount.getId(), request.getDistributorId());
        
        return savedAccount;
    }

    /**
     * Get ledger account by company and distributor ID
     */
    @Transactional(readOnly = true)
    public LedgerAccount getLedgerAccount(Long companyId, Long distributorId) {
        return ledgerAccountRepository.findByCompanyIdAndDistributorId(companyId, distributorId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger account not found"));
    }

    /**
     * Get ledger account by ID
     */
    @Transactional(readOnly = true)
    public LedgerAccount getLedgerAccountById(Long accountId) {
        return ledgerAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger account not found"));
    }

    /**
     * Get ledger account with lock for balance updates
     */
    public LedgerAccount getLedgerAccountWithLock(Long accountId) {
        return ledgerAccountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger account not found"));
    }

    /**
     * Get ledger summary for an account
     */
    @Transactional(readOnly = true)
    public LedgerSummaryResponse getLedgerSummary(Long accountId) {
        LedgerAccount account = getLedgerAccountById(accountId);
        
        BigDecimal totalDebits = ledgerTransactionRepository.calculateTotalDebits(accountId);
        BigDecimal totalCredits = ledgerTransactionRepository.calculateTotalCredits(accountId);
        long transactionCount = ledgerTransactionRepository.countByLedgerAccountId(accountId);

        LedgerSummaryResponse summary = new LedgerSummaryResponse();
        summary.setAccountId(account.getId());
        summary.setAccountNumber(account.getAccountNumber());
        summary.setAccountName(account.getAccountName());
        summary.setTotalDebits(totalDebits);
        summary.setTotalCredits(totalCredits);
        summary.setNetBalance(totalCredits.subtract(totalDebits));
        summary.setClosingBalance(account.getCurrentBalance());
        summary.setCreditLimit(account.getCreditLimit());
        summary.setTransactionCount(transactionCount);
        summary.setStatus(account.getStatus().name());

        return summary;
    }

    /**
     * Update account status
     */
    public void updateAccountStatus(Long accountId, AccountStatus status, String updatedBy) {
        LedgerAccount account = getLedgerAccountById(accountId);
        account.setStatus(status);
        account.setUpdatedBy(updatedBy);
        ledgerAccountRepository.save(account);
        
        log.info("Updated account {} status to {}", accountId, status);
    }

    /**
     * Update credit limit
     */
    public void updateCreditLimit(Long accountId, BigDecimal creditLimit, String updatedBy) {
        LedgerAccount account = getLedgerAccountById(accountId);
        account.setCreditLimit(creditLimit);
        account.setUpdatedBy(updatedBy);
        ledgerAccountRepository.save(account);
        
        log.info("Updated account {} credit limit to {}", accountId, creditLimit);
    }

    /**
     * Get all accounts for a company
     */
    @Transactional(readOnly = true)
    public List<LedgerAccount> getAccountsByCompany(Long companyId) {
        return ledgerAccountRepository.findByCompanyId(companyId);
    }

    /**
     * Check if account can be debited
     */
    @Transactional(readOnly = true)
    public boolean canDebitAccount(Long accountId, BigDecimal amount) {
        LedgerAccount account = getLedgerAccountById(accountId);
        return account.canDebit(amount);
    }
}