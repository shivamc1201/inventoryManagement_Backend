package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.interceptors.reports.dto.OutstandingSummaryResponse;
import com.nector.userservice.interceptors.reports.dto.OutstandingSummaryRowDto;
import com.nector.userservice.interceptors.reports.dto.ReceivablesAgeingDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.service.ReceivablesReportService;
import com.nector.userservice.model.DealerLedgerTransaction;
import com.nector.userservice.model.PaymentApproval;
import com.nector.userservice.repository.DealerLedgerTransactionRepository;
import com.nector.userservice.repository.InvoiceRepository;
import com.nector.userservice.repository.PaymentApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceivablesReportServiceImpl implements ReceivablesReportService {

    private final DealerLedgerTransactionRepository dealerLedgerRepo;
    private final PaymentApprovalRepository paymentApprovalRepository;
    private final DistributorRepository distributorRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public List<ReceivablesAgeingDto> getOutstanding(Long distributorId) {
        List<Object[]> overview = dealerLedgerRepo.getDistributorOverview(distributorId);
        return overview.stream().map(r -> {
            Long dealerId = ((Number) r[0]).longValue();
            String dealerName = (String) r[1];
            BigDecimal balance = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
            BigDecimal totalDebit = dealerLedgerRepo.sumDebitsByDealer(dealerId, distributorId);
            BigDecimal totalCredit = dealerLedgerRepo.sumCreditsByDealer(dealerId, distributorId);
            return ReceivablesAgeingDto.builder()
                    .dealerId(dealerId)
                    .dealerName(dealerName)
                    .currentBalance(balance)
                    .totalDebit(totalDebit != null ? totalDebit : BigDecimal.ZERO)
                    .totalCredit(totalCredit != null ? totalCredit : BigDecimal.ZERO)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getAgeingBuckets(Long distributorId) {
        List<DealerLedgerTransaction> txns = dealerLedgerRepo.findByOptionalDistributorId(distributorId);
        LocalDate today = LocalDate.now();
        BigDecimal b0to30 = BigDecimal.ZERO, b31to60 = BigDecimal.ZERO,
                b61to90 = BigDecimal.ZERO, b90plus = BigDecimal.ZERO;
        for (DealerLedgerTransaction t : txns) {
            if (t.getBalance() == null || t.getDate() == null) continue;
            long days = java.time.temporal.ChronoUnit.DAYS.between(t.getDate(), today);
            if (days <= 30) b0to30 = b0to30.add(t.getBalance());
            else if (days <= 60) b31to60 = b31to60.add(t.getBalance());
            else if (days <= 90) b61to90 = b61to90.add(t.getBalance());
            else b90plus = b90plus.add(t.getBalance());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("0-30", b0to30);
        result.put("31-60", b31to60);
        result.put("61-90", b61to90);
        result.put("90+", b90plus);
        return result;
    }

    @Override
    public List<Map<String, Object>> getCollectionHistory(ReportFilterRequest filter) {
        LocalDateTime from = (filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusMonths(3)).atStartOfDay();
        LocalDateTime to = (filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now()).atTime(23, 59, 59);
        List<PaymentApproval> payments = filter.getDistributorId() != null
                ? paymentApprovalRepository.findByDistributorIdAndStatusOrderByCreatedAtDesc(
                        filter.getDistributorId(), "LEDGER_UPDATED")
                : paymentApprovalRepository.findByStatusOrderByCreatedAtDesc("LEDGER_UPDATED");
        return payments.stream()
                .filter(p -> p.getApprovedAt() != null
                        && !p.getApprovedAt().isBefore(from) && !p.getApprovedAt().isAfter(to))
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("distributorId", p.getDistributorId());
                    String distName = p.getDistributorId() != null
                            ? distributorRepository.findById(p.getDistributorId())
                                    .map(d -> d.getFirstName() + " " + d.getLastName())
                                    .orElse(null)
                            : null;
                    m.put("distributorName", distName);
                    m.put("amount", p.getAmount());
                    m.put("description", p.getDescription());
                    m.put("approvedAt", p.getApprovedAt());
                    return m;
                }).collect(Collectors.toList());
    }

    @Override
    public OutstandingSummaryResponse getOutstandingSummary() {
        // All invoices per distributor (no date filter)
        List<Object[]> invoiceRows = invoiceRepository.getAllInvoiceSummaryByDistributor();

        // Build payment map: distributorId -> total collected (all time)
        Map<Long, BigDecimal> paymentMap = new HashMap<>();
        for (Object[] r : paymentApprovalRepository.sumAllApprovedPaymentsGroupedByDistributor()) {
            Long distId = r[0] != null ? ((Number) r[0]).longValue() : null;
            BigDecimal paid = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
            if (distId != null) paymentMap.put(distId, paid);
        }

        LocalDate today = LocalDate.now();
        List<OutstandingSummaryRowDto> rows = new ArrayList<>();
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        BigDecimal collectedTotal = BigDecimal.ZERO;
        long totalDays = 0;
        int countWithOutstanding = 0;

        for (Object[] r : invoiceRows) {
            Long distId = r[0] != null ? ((Number) r[0]).longValue() : null;
            String distName = r[1] != null ? r[1].toString() : "Unknown";
            long invoiceCount = r[2] != null ? ((Number) r[2]).longValue() : 0L;
            BigDecimal totalSales = r[3] != null ? new BigDecimal(r[3].toString()) : BigDecimal.ZERO;
            LocalDateTime oldestInvoiceDt = r[4] != null ? (LocalDateTime) r[4] : null;

            BigDecimal collected = distId != null ? paymentMap.getOrDefault(distId, BigDecimal.ZERO) : BigDecimal.ZERO;
            BigDecimal outstanding = totalSales.subtract(collected).max(BigDecimal.ZERO);

            collectedTotal = collectedTotal.add(collected);

            if (outstanding.compareTo(BigDecimal.ZERO) == 0) continue;

            long daysOverdue = oldestInvoiceDt != null
                    ? ChronoUnit.DAYS.between(oldestInvoiceDt.toLocalDate(), today)
                    : 0L;

            String status = daysOverdue > 60 ? "Overdue" : daysOverdue >= 20 ? "Watch" : "Good";

            rows.add(OutstandingSummaryRowDto.builder()
                    .distributorId(distId)
                    .distributorName(distName)
                    .totalInvoices(invoiceCount)
                    .totalSales(totalSales)
                    .collectedAmount(collected)
                    .outstandingAmount(outstanding)
                    .daysOverdue(daysOverdue)
                    .status(status)
                    .build());

            totalOutstanding = totalOutstanding.add(outstanding);
            if (daysOverdue > 60) overdueAmount = overdueAmount.add(outstanding);
            totalDays += daysOverdue;
            countWithOutstanding++;
        }

        rows.sort((a, b) -> b.getOutstandingAmount().compareTo(a.getOutstandingAmount()));

        long avgDso = countWithOutstanding > 0 ? totalDays / countWithOutstanding : 0L;

        return OutstandingSummaryResponse.builder()
                .totalOutstanding(totalOutstanding)
                .overdueAmount(overdueAmount)
                .collectedTotal(collectedTotal)
                .avgDso(avgDso)
                .rows(rows)
                .build();
    }
}
