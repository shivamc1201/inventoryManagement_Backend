package com.nector.userservice.transaction.config;

import com.nector.userservice.transaction.entity.TransactionFund;
import com.nector.userservice.transaction.entity.TransactionLedger;
import com.nector.userservice.transaction.entity.TransactionVoucher;
import com.nector.userservice.transaction.enums.FundLocation;
import com.nector.userservice.transaction.enums.LedgerType;
import com.nector.userservice.transaction.enums.PaymentMode;
import com.nector.userservice.transaction.enums.UnderGroup;
import com.nector.userservice.transaction.enums.VoucherStatus;
import com.nector.userservice.transaction.repository.TransactionFundRepository;
import com.nector.userservice.transaction.repository.TransactionLedgerRepository;
import com.nector.userservice.transaction.repository.TransactionVoucherRepository;
import com.nector.userservice.transaction.service.TransactionNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Seeds the Transaction Master expense/income ledgers (spec §11) and a small
 * set of sample vouchers/funds (the worked example in spec §9) so the
 * Transaction Cashbook page renders populated out of the box.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionDataSeeder implements CommandLineRunner {

    private static final List<String> DIRECT_EXPENSE_LEDGERS = List.of(
            "Wages", "Spare Parts", "Unloading Charge", "Loading Charge", "Freight Outward", "Tea", "Stationery");

    private static final List<String> INDIRECT_EXPENSE_LEDGERS = List.of(
            "Fooding Exp", "Maintenance Machinery", "Maintenance Electricity", "Freight Inward",
            "Courier Exp", "Office Maintenance", "MIS Expense");

    private static final List<String> DIRECT_INCOME_LEDGERS = List.of("Sale From Scrap", "Other Receive");

    private final TransactionLedgerRepository ledgerRepository;
    private final TransactionVoucherRepository voucherRepository;
    private final TransactionFundRepository fundRepository;
    private final TransactionNumberService numberService;

    @Override
    public void run(String... args) {
        if (ledgerRepository.count() == 0) {
            seedLedgers();
        }
        if (voucherRepository.count() == 0 && fundRepository.count() == 0) {
            seedSampleData();
        }
    }

    private void seedLedgers() {
        LocalDate today = LocalDate.now();

        DIRECT_EXPENSE_LEDGERS.forEach(name -> saveLedger(name, LedgerType.EXPENSE, UnderGroup.DIRECT_EXPENSE, today));
        INDIRECT_EXPENSE_LEDGERS.forEach(name -> saveLedger(name, LedgerType.EXPENSE, UnderGroup.INDIRECT_EXPENSE, today));
        DIRECT_INCOME_LEDGERS.forEach(name -> saveLedger(name, LedgerType.INCOME, UnderGroup.DIRECT_INCOME, today));

        log.info("Seeded {} transaction ledgers", ledgerRepository.count());
    }

    private void saveLedger(String name, LedgerType ledgerType, UnderGroup underGroup, LocalDate createdAt) {
        TransactionLedger ledger = new TransactionLedger();
        ledger.setLedgerName(name);
        ledger.setLedgerType(ledgerType);
        ledger.setUnderGroup(underGroup);
        ledger.setCreatedAt(createdAt);
        ledgerRepository.save(ledger);
    }

    private void seedSampleData() {
        Map<String, TransactionLedger> ledgersByName = ledgerRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(TransactionLedger::getLedgerName, l -> l));

        LocalDate sampleDate = LocalDate.of(2026, 6, 1);

        saveVoucher(ledgersByName.get("Wages"), sampleDate, "Suresh", "9876543210", "BILL-22",
                PaymentMode.CASH, "", new BigDecimal("400"), "Being Amount Paid to Suresh Against Wages");

        saveVoucher(ledgersByName.get("Tea"), sampleDate, "", "", "",
                PaymentMode.CASH, "", new BigDecimal("100"), "Being Amount Paid for Tea Expenses");

        saveVoucher(ledgersByName.get("Sale From Scrap"), sampleDate, "", "", "",
                PaymentMode.UPI, "UPI24061500001", new BigDecimal("15000"), "Being Amount Received Against Sale of Scrap");

        saveFund(sampleDate, new BigDecimal("10000"), PaymentMode.UPI, "UPI24061500123",
                FundLocation.OFFICE, "Fund received from HO");
        saveFund(sampleDate, new BigDecimal("5000"), PaymentMode.CASH, "",
                FundLocation.OFFICE, "Fund received from HO");

        log.info("Seeded sample transaction vouchers and funds");
    }

    private void saveVoucher(TransactionLedger ledger, LocalDate date, String partyName, String mobileNo,
            String invoiceRef, PaymentMode paymentMode, String transactionId, BigDecimal amount, String narration) {
        if (ledger == null) {
            return;
        }
        TransactionVoucher voucher = new TransactionVoucher();
        voucher.setVoucherNo(numberService.nextVoucherNo());
        voucher.setDate(date);
        voucher.setVoucherType(ledger.getLedgerType());
        voucher.setLedgerId(ledger.getId());
        voucher.setLedgerName(ledger.getLedgerName());
        voucher.setPartyName(partyName);
        voucher.setMobileNo(mobileNo);
        voucher.setInvoiceRef(invoiceRef);
        voucher.setPaymentMode(paymentMode);
        voucher.setTransactionId(transactionId);
        voucher.setAmount(amount);
        voucher.setLessAdjustment(BigDecimal.ZERO);
        voucher.setNarration(narration);
        voucher.setStatus(VoucherStatus.APPROVED);
        voucher.setCreatedAt(date);
        voucherRepository.save(voucher);
    }

    private void saveFund(LocalDate date, BigDecimal amount, PaymentMode paymentMode, String transactionId,
            FundLocation location, String narration) {
        TransactionFund fund = new TransactionFund();
        fund.setFundNo(numberService.nextFundNo());
        fund.setDate(date);
        fund.setAmount(amount);
        fund.setPaymentMode(paymentMode);
        fund.setTransactionId(transactionId);
        fund.setLocation(location);
        fund.setNarration(narration);
        fund.setCreatedAt(date);
        fundRepository.save(fund);
    }
}
