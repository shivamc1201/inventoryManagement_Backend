package com.nector.userservice.transaction.service;

import com.nector.userservice.transaction.dto.CashbookEntry;
import com.nector.userservice.transaction.dto.CashbookSummary;
import com.nector.userservice.transaction.entity.TransactionLedger;
import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.UnderGroup;
import com.nector.userservice.transaction.repository.LedgerCashbookAggregate;
import com.nector.userservice.transaction.repository.TransactionFundRepository;
import com.nector.userservice.transaction.repository.TransactionLedgerRepository;
import com.nector.userservice.transaction.repository.TransactionVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CashbookService {

    private final TransactionVoucherRepository voucherRepository;
    private final TransactionFundRepository fundRepository;
    private final TransactionLedgerRepository ledgerRepository;

    public CashbookSummary getCashbook(LocalDate from, LocalDate to) {
        List<CashbookEntry> entries = new ArrayList<>();

        for (LedgerCashbookAggregate aggregate : voucherRepository.aggregateByLedgerBetween(from, to)) {
            TransactionLedger ledger = ledgerRepository.findByLedgerNameIgnoreCase(aggregate.getLedgerName()).orElse(null);
            LedgerType ledgerType = ledger != null ? ledger.getLedgerType() : null;
            UnderGroup underGroup = ledger != null ? ledger.getUnderGroup() : null;

            BigDecimal gross = aggregate.getGrossAmount();
            BigDecimal lessAdjustment = aggregate.getLessAdjustment();
            BigDecimal net = gross.subtract(lessAdjustment);

            entries.add(new CashbookEntry(aggregate.getLedgerName(), ledgerType, underGroup, gross, lessAdjustment, net));
        }

        BigDecimal fundsInRange = fundRepository.sumAmountBetween(from, to);
        if (fundsInRange.compareTo(BigDecimal.ZERO) > 0) {
            entries.add(new CashbookEntry(
                    TransactionLedgerService.RECEIVED_FROM_HO,
                    LedgerType.INCOME,
                    UnderGroup.DIRECT_INCOME,
                    fundsInRange,
                    BigDecimal.ZERO,
                    fundsInRange));
        }

        BigDecimal totalIncome = sumByType(entries, LedgerType.INCOME);
        BigDecimal totalExpense = sumByType(entries, LedgerType.EXPENSE);

        BigDecimal openingBalance = computeOpeningBalance(from);
        BigDecimal closingBalance = openingBalance.add(totalIncome).subtract(totalExpense);

        return new CashbookSummary(openingBalance, closingBalance, from, to, entries);
    }

    private BigDecimal computeOpeningBalance(LocalDate from) {
        BigDecimal priorIncome = voucherRepository.sumNetAmountByTypeBefore(LedgerType.INCOME, from);
        BigDecimal priorExpense = voucherRepository.sumNetAmountByTypeBefore(LedgerType.EXPENSE, from);
        BigDecimal priorFunds = fundRepository.sumAmountBefore(from);

        return priorIncome.add(priorFunds).subtract(priorExpense);
    }

    private BigDecimal sumByType(List<CashbookEntry> entries, LedgerType ledgerType) {
        return entries.stream()
                .filter(entry -> entry.getLedgerType() == ledgerType)
                .map(CashbookEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
