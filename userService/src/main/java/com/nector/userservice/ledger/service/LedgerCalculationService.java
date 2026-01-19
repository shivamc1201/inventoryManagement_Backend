package com.nector.userservice.ledger.service;

import com.nector.userservice.ledger.entity.LedgerAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for ledger calculations
 * Handles balance calculations and financial computations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerCalculationService {

    /**
     * Calculate running balance after a transaction
     * Balance = Current Balance + Credit Amount - Debit Amount
     */
    public BigDecimal calculateRunningBalance(LedgerAccount account, BigDecimal debitAmount, BigDecimal creditAmount) {
        BigDecimal currentBalance = account.getCurrentBalance();
        BigDecimal newBalance = currentBalance.add(creditAmount).subtract(debitAmount);
        
        log.debug("Balance calculation - Current: {}, Debit: {}, Credit: {}, New: {}", 
                currentBalance, debitAmount, creditAmount, newBalance);
        
        return newBalance;
    }

    /**
     * Validate if account can be debited with the given amount
     */
    public boolean canDebitAccount(LedgerAccount account, BigDecimal debitAmount) {
        BigDecimal newBalance = account.getCurrentBalance().subtract(debitAmount);
        BigDecimal availableCredit = newBalance.add(account.getCreditLimit());
        
        return availableCredit.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Calculate available credit for an account
     */
    public BigDecimal calculateAvailableCredit(LedgerAccount account) {
        return account.getCurrentBalance().add(account.getCreditLimit());
    }

    /**
     * Check if account is over credit limit
     */
    public boolean isOverCreditLimit(LedgerAccount account) {
        return calculateAvailableCredit(account).compareTo(BigDecimal.ZERO) < 0;
    }
}