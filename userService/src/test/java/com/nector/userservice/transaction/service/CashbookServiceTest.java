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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashbookServiceTest {

    @Mock
    private TransactionVoucherRepository voucherRepository;

    @Mock
    private TransactionFundRepository fundRepository;

    @Mock
    private TransactionLedgerRepository ledgerRepository;

    private CashbookService cashbookService;

    @BeforeEach
    void setUp() {
        cashbookService = new CashbookService(voucherRepository, fundRepository, ledgerRepository);
    }

    /**
     * Worked example from spec §9:
     * VCH-001 EXPENSE Wages CASH amount 400, lessAdjustment 0 -> net 400
     * VCH-002 EXPENSE Tea CASH amount 100, lessAdjustment 0 -> net 100
     * VCH-003 INCOME "Sale From Scrap" UPI amount 15000 -> net 15000
     * FND-001 amount 10000, FND-002 amount 5000
     *
     * GET /cashbook?from=2026-06-01&to=2026-06-30 must yield:
     * openingBalance = 0, totalIncome = 30000, totalExpense = 500, closingBalance = 29500
     */
    @Test
    void juneCashbookBalancesPerWorkedExample() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        when(voucherRepository.aggregateByLedgerBetween(from, to)).thenReturn(List.of(
                aggregate("Wages", new BigDecimal("400"), BigDecimal.ZERO),
                aggregate("Tea", new BigDecimal("100"), BigDecimal.ZERO),
                aggregate("Sale From Scrap", new BigDecimal("15000"), BigDecimal.ZERO)));

        when(ledgerRepository.findByLedgerNameIgnoreCase("Wages"))
                .thenReturn(Optional.of(ledger("Wages", LedgerType.EXPENSE, UnderGroup.DIRECT_EXPENSE)));
        when(ledgerRepository.findByLedgerNameIgnoreCase("Tea"))
                .thenReturn(Optional.of(ledger("Tea", LedgerType.EXPENSE, UnderGroup.DIRECT_EXPENSE)));
        when(ledgerRepository.findByLedgerNameIgnoreCase("Sale From Scrap"))
                .thenReturn(Optional.of(ledger("Sale From Scrap", LedgerType.INCOME, UnderGroup.DIRECT_INCOME)));

        when(fundRepository.sumAmountBetween(from, to)).thenReturn(new BigDecimal("15000"));
        when(fundRepository.sumAmountBefore(from)).thenReturn(BigDecimal.ZERO);
        when(voucherRepository.sumNetAmountByTypeBefore(eq(LedgerType.INCOME), eq(from))).thenReturn(BigDecimal.ZERO);
        when(voucherRepository.sumNetAmountByTypeBefore(eq(LedgerType.EXPENSE), eq(from))).thenReturn(BigDecimal.ZERO);

        CashbookSummary summary = cashbookService.getCashbook(from, to);

        assertThat(summary.getOpeningBalance()).isEqualByComparingTo("0");
        assertThat(summary.getClosingBalance()).isEqualByComparingTo("29500");
        assertThat(summary.getFromDate()).isEqualTo(from);
        assertThat(summary.getToDate()).isEqualTo(to);

        assertThat(summary.getEntries()).hasSize(4);

        CashbookEntry wages = findEntry(summary, "Wages");
        assertThat(wages.getLedgerType()).isEqualTo(LedgerType.EXPENSE);
        assertThat(wages.getAmount()).isEqualByComparingTo("400");

        CashbookEntry tea = findEntry(summary, "Tea");
        assertThat(tea.getAmount()).isEqualByComparingTo("100");

        CashbookEntry saleFromScrap = findEntry(summary, "Sale From Scrap");
        assertThat(saleFromScrap.getLedgerType()).isEqualTo(LedgerType.INCOME);
        assertThat(saleFromScrap.getAmount()).isEqualByComparingTo("15000");

        CashbookEntry receivedFromHo = findEntry(summary, "Received From HO");
        assertThat(receivedFromHo.getLedgerType()).isEqualTo(LedgerType.INCOME);
        assertThat(receivedFromHo.getUnderGroup()).isEqualTo(UnderGroup.DIRECT_INCOME);
        assertThat(receivedFromHo.getGrossAmount()).isEqualByComparingTo("15000");
        assertThat(receivedFromHo.getAmount()).isEqualByComparingTo("15000");

        // Balance rule (§6.4): leftTotal = totalExpense + closingBalance, rightTotal = openingBalance + totalIncome
        BigDecimal totalIncome = new BigDecimal("30000");
        BigDecimal totalExpense = new BigDecimal("500");
        BigDecimal rightTotal = summary.getOpeningBalance().add(totalIncome);
        BigDecimal leftTotal = totalExpense.add(summary.getClosingBalance());
        assertThat(leftTotal).isEqualByComparingTo(rightTotal);
        assertThat(rightTotal).isEqualByComparingTo("30000");
    }

    /**
     * Worked example from spec §9, second call:
     * GET /cashbook?from=2026-07-01&to=2026-07-31 (no July rows) must yield
     * openingBalance = 29500 (carry-forward), empty entries, closingBalance = 29500.
     */
    @Test
    void julyCashbookCarriesForwardOpeningBalance() {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 31);

        when(voucherRepository.aggregateByLedgerBetween(from, to)).thenReturn(List.of());
        when(fundRepository.sumAmountBetween(from, to)).thenReturn(BigDecimal.ZERO);

        // Prior-period (June) totals: income (incl. funds) 30000, expense 500 -> opening = 29500
        when(voucherRepository.sumNetAmountByTypeBefore(eq(LedgerType.INCOME), eq(from))).thenReturn(new BigDecimal("15000"));
        when(voucherRepository.sumNetAmountByTypeBefore(eq(LedgerType.EXPENSE), eq(from))).thenReturn(new BigDecimal("500"));
        when(fundRepository.sumAmountBefore(from)).thenReturn(new BigDecimal("15000"));

        CashbookSummary summary = cashbookService.getCashbook(from, to);

        assertThat(summary.getOpeningBalance()).isEqualByComparingTo("29500");
        assertThat(summary.getEntries()).isEmpty();
        assertThat(summary.getClosingBalance()).isEqualByComparingTo("29500");
    }

    private CashbookEntry findEntry(CashbookSummary summary, String ledgerName) {
        return summary.getEntries().stream()
                .filter(e -> e.getLedgerName().equals(ledgerName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Entry not found: " + ledgerName));
    }

    private TransactionLedger ledger(String name, LedgerType type, UnderGroup underGroup) {
        TransactionLedger ledger = new TransactionLedger();
        ledger.setLedgerName(name);
        ledger.setLedgerType(type);
        ledger.setUnderGroup(underGroup);
        ledger.setCreatedAt(LocalDate.now());
        return ledger;
    }

    private LedgerCashbookAggregate aggregate(String ledgerName, BigDecimal gross, BigDecimal lessAdjustment) {
        return new LedgerCashbookAggregate() {
            @Override
            public String getLedgerName() {
                return ledgerName;
            }

            @Override
            public BigDecimal getGrossAmount() {
                return gross;
            }

            @Override
            public BigDecimal getLessAdjustment() {
                return lessAdjustment;
            }
        };
    }
}
