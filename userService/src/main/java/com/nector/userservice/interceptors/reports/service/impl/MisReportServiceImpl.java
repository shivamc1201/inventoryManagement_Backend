package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.bom.repository.RawMaterialInventoryLotRepository;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.interceptors.reports.dto.DistributorOutstandingRowDto;
import com.nector.userservice.interceptors.reports.dto.MisDashboardResponse;
import com.nector.userservice.interceptors.reports.dto.MisKpiCardDto;
import com.nector.userservice.interceptors.reports.dto.MisKpiSummaryDto;
import com.nector.userservice.interceptors.reports.dto.MisRegionPerformanceDto;
import com.nector.userservice.interceptors.reports.dto.MisSalesTargetDto;
import com.nector.userservice.interceptors.reports.dto.MisSummaryDto;
import com.nector.userservice.interceptors.reports.dto.ProductSalesRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.SalesInvoiceRowDto;
import com.nector.userservice.interceptors.reports.service.InventoryReportService;
import com.nector.userservice.interceptors.reports.service.MisReportService;
import com.nector.userservice.interceptors.reports.service.ReceivablesReportService;
import com.nector.userservice.model.Invoice;
import com.nector.userservice.repository.FinishedProductRepository;
import com.nector.userservice.repository.InvoiceLineItemRepository;
import com.nector.userservice.repository.InvoiceRepository;
import com.nector.userservice.repository.PaymentApprovalRepository;
import com.nector.userservice.repository.SalesPersonRepository;
import com.nector.userservice.dispatch.repository.GdnRepository;
import com.nector.userservice.ordertracking.repository.OrderTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MisReportServiceImpl implements MisReportService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final PaymentApprovalRepository paymentApprovalRepository;
    private final GdnRepository gdnRepository;
    private final OrderTrackingRepository orderTrackingRepository;
    private final RawMaterialInventoryLotRepository lotRepository;
    private final InventoryReportService inventoryReportService;
    private final ReceivablesReportService receivablesReportService;
    private final SalesPersonRepository salesPersonRepository;
    private final DistributorRepository distributorRepository;

    // ─── Standalone KPI summary endpoint ─────────────────────────────────────

    @Override
    public MisKpiSummaryDto getKpiSummary(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);

        BigDecimal totalSales = sumSales(filter.getDistributorId(), null, from, to);
        BigDecimal totalPurchase = lotRepository.sumPurchaseValueByDateRange(
                from.toInstant(ZoneOffset.UTC), to.toInstant(ZoneOffset.UTC));
        BigDecimal currentStockValue = totalStockValue();
        BigDecimal totalCollections = filter.getDistributorId() != null
                ? paymentApprovalRepository.sumApprovedPaymentsByDistributorAndDateRange(
                        filter.getDistributorId(), from, to)
                : BigDecimal.ZERO;

        long openOrders = orderTrackingRepository.countPending();
        List<Object[]> dispatchRows = gdnRepository.getDispatchSummary(from, to);
        long dispatchVol = (!dispatchRows.isEmpty() && dispatchRows.get(0)[0] != null)
                ? ((Number) dispatchRows.get(0)[0]).longValue() : 0L;

        List<Object[]> trendRows = invoiceRepository.getMonthlySalesTrend(filter.getDistributorId(), from, to);
        List<Map<String, Object>> trend = trendRows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("month", r[0]); m.put("year", r[1]); m.put("total", r[2]); m.put("count", r[3]);
            return m;
        }).collect(Collectors.toList());

        List<Object[]> rankRows = invoiceRepository.getSalesByDistributor(from, to, PageRequest.of(0, 10));
        List<Map<String, Object>> ranking = rankRows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("distributorId", r[0]); m.put("distributorName", r[1]);
            m.put("orderCount", r[2]); m.put("totalValue", r[3]);
            return m;
        }).collect(Collectors.toList());

        return MisKpiSummaryDto.builder()
                .totalSalesValue(totalSales != null ? totalSales : BigDecimal.ZERO)
                .totalPurchase(totalPurchase != null ? totalPurchase : BigDecimal.ZERO)
                .currentStockValue(currentStockValue)
                .totalCollections(totalCollections != null ? totalCollections : BigDecimal.ZERO)
                .openOrdersCount(openOrders)
                .dispatchVolume(dispatchVol)
                .monthlyTrend(trend)
                .distributorRanking(ranking)
                .build();
    }

    @Override
    public List<SalesInvoiceRowDto> getRecentInvoices(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return invoiceRepository.findByDistributorAndDateRange(
                filter.getDistributorId(), from, to, PageRequest.of(0, 10))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    // ─── Sales target (weekly actuals, target = null until table exists) ──────

    @Override
    public List<MisSalesTargetDto> getSalesTarget(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        List<Long> distIds = resolveDistributorIds(filter);

        List<Object[]> rows = distIds != null
                ? invoiceRepository.getWeeklySalesForDistributors(distIds, from, to)
                : invoiceRepository.getWeeklySalesSummary(from, to, filter.getDistributorId());

        Map<Integer, BigDecimal> weeklyActuals = new HashMap<>();
        for (Object[] r : rows) {
            int weekNum = r[0] != null ? ((Number) r[0]).intValue() : 1;
            BigDecimal total = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
            weeklyActuals.put(weekNum, total);
        }
        List<MisSalesTargetDto> result = new ArrayList<>();
        for (int w = 1; w <= 5; w++) {
            result.add(MisSalesTargetDto.builder()
                    .period("Week " + w)
                    .actual(weeklyActuals.getOrDefault(w, BigDecimal.ZERO))
                    .target(null)
                    .build());
        }
        return result;
    }

    // ─── Region performance ───────────────────────────────────────────────────

    @Override
    public List<MisRegionPerformanceDto> getRegionPerformance(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return invoiceRepository.getSalesByRegion(from, to).stream().map(r ->
                MisRegionPerformanceDto.builder()
                        .region(r[0] != null ? r[0].toString() : "Unknown")
                        .totalSales(r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO)
                        .invoiceCount(r[2] != null ? ((Number) r[2]).longValue() : 0L)
                        .distributorCount(r[3] != null ? ((Number) r[3]).longValue() : 0L)
                        .build()
        ).collect(Collectors.toList());
    }

    // ─── Consolidated dashboard ───────────────────────────────────────────────

    @Override
    public MisDashboardResponse buildDashboard(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);

        // Resolve effective distributor IDs from region + distributorId filters
        List<Long> distIds = resolveDistributorIds(filter);
        Long singleDistId = (distIds != null && distIds.size() == 1) ? distIds.get(0) : filter.getDistributorId();

        // Previous period for change% (same duration, shifted back)
        long periodDays = ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate()) + 1;
        LocalDateTime prevFrom = from.minusDays(periodDays);
        LocalDateTime prevTo = from.minusSeconds(1);

        // ── KPI cards ──
        BigDecimal curSales = orZero(distIds != null
                ? invoiceRepository.sumGrandTotalForDistributors(distIds, from, to)
                : invoiceRepository.sumGrandTotalByDistributorAndDateRange(singleDistId, from, to));

        BigDecimal prevSales = orZero(distIds != null
                ? invoiceRepository.sumGrandTotalForDistributors(distIds, prevFrom, prevTo)
                : invoiceRepository.sumGrandTotalByDistributorAndDateRange(singleDistId, prevFrom, prevTo));

        BigDecimal curPurchase = orZero(lotRepository.sumPurchaseValueByDateRange(
                from.toInstant(ZoneOffset.UTC), to.toInstant(ZoneOffset.UTC)));
        BigDecimal prevPurchase = orZero(lotRepository.sumPurchaseValueByDateRange(
                prevFrom.toInstant(ZoneOffset.UTC), prevTo.toInstant(ZoneOffset.UTC)));

        BigDecimal stockValue = totalStockValue();

        Map<String, Object> agingMap = receivablesReportService.getAgeingBuckets(singleDistId);
        BigDecimal totalOutstanding = agingMap.values().stream()
                .map(v -> v instanceof BigDecimal ? (BigDecimal) v : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MisSummaryDto summary = MisSummaryDto.builder()
                .totalSales(kpiCard(curSales, prevSales))
                .totalPurchase(kpiCard(curPurchase, prevPurchase))
                .currentStockValue(MisKpiCardDto.builder().amount(stockValue).changePercentage(null).comparedTo(null).build())
                .outstandingReceivable(MisKpiCardDto.builder().amount(totalOutstanding).changePercentage(null).comparedTo(null).build())
                .build();

        // ── Weekly sales target ──
        List<Object[]> weekRows = distIds != null
                ? invoiceRepository.getWeeklySalesForDistributors(distIds, from, to)
                : invoiceRepository.getWeeklySalesSummary(from, to, singleDistId);
        List<MisSalesTargetDto> salesTarget = buildWeeklyTarget(weekRows);

        // ── Top 5 products ──
        List<ProductSalesRowDto> topProducts = buildProductRows(distIds, from, to, true).stream()
                .limit(5).collect(Collectors.toList());

        // ── Stock by category ──
        List<Map<String, Object>> stockByCategory = inventoryReportService.getSnapshot(null).stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCategory() != null ? s.getCategory() : "OTHER",
                        Collectors.reducing(BigDecimal.ZERO,
                                s -> s.getTotalValue() != null ? s.getTotalValue() : BigDecimal.ZERO,
                                BigDecimal::add)))
                .entrySet().stream()
                .map(e -> { Map<String, Object> m = new HashMap<>(); m.put("categoryName", e.getKey()); m.put("stockValue", e.getValue()); return m; })
                .sorted((a, b) -> ((BigDecimal) b.get("stockValue")).compareTo((BigDecimal) a.get("stockValue")))
                .collect(Collectors.toList());

        // ── Receivable aging ──
        Map<String, Object> receivableAging = receivablesReportService.getAgeingBuckets(singleDistId);

        // ── Product performance table (all products, ranked) ──
        List<ProductSalesRowDto> productPerformance = buildProductRows(distIds, from, to, false);

        // ── Region performance tab ──
        List<MisRegionPerformanceDto> regionPerformance = invoiceRepository.getSalesByRegion(from, to).stream()
                .map(r -> MisRegionPerformanceDto.builder()
                        .region(r[0] != null ? r[0].toString() : "Unknown")
                        .totalSales(r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO)
                        .invoiceCount(r[2] != null ? ((Number) r[2]).longValue() : 0L)
                        .distributorCount(r[3] != null ? ((Number) r[3]).longValue() : 0L)
                        .build())
                .collect(Collectors.toList());

        // ── Distributor outstanding tab ──
        List<DistributorOutstandingRowDto> distributorOutstanding = buildDistributorOutstanding(distIds, singleDistId, from, to);

        return MisDashboardResponse.builder()
                .summary(summary)
                .salesTarget(salesTarget)
                .topProducts(topProducts)
                .stockByCategory(stockByCategory)
                .receivableAging(receivableAging)
                .productPerformance(productPerformance)
                .regionPerformance(regionPerformance)
                .distributorOutstanding(distributorOutstanding)
                .build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Resolves the effective list of distributor IDs when a region filter is active.
     * Returns null when no region filter — callers should then use single distributorId (may also be null = all).
     */
    private List<Long> resolveDistributorIds(ReportFilterRequest filter) {
        if (filter.getRegion() == null || filter.getRegion().isBlank()) return null;
        List<Long> spIds = salesPersonRepository.findByRegion(filter.getRegion())
                .stream().map(sp -> sp.getId()).collect(Collectors.toList());
        if (spIds.isEmpty()) return List.of(-1L); // region exists but no salespersons → no results
        List<Long> distIds = distributorRepository.findIdsBySalespersonIdIn(spIds);
        if (filter.getDistributorId() != null && distIds.contains(filter.getDistributorId())) {
            return List.of(filter.getDistributorId()); // distributor filter within region
        }
        return distIds.isEmpty() ? List.of(-1L) : distIds;
    }

    private List<ProductSalesRowDto> buildProductRows(List<Long> distIds, LocalDateTime from, LocalDateTime to, boolean quantityOrder) {
        List<Object[]> rows = distIds != null
                ? invoiceLineItemRepository.getProductSalesSummaryForDistributors(from, to, distIds)
                : invoiceLineItemRepository.getProductSalesSummary(from, to);

        BigDecimal grandTotal = rows.stream()
                .map(r -> r[3] != null ? new BigDecimal(r[3].toString()) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ProductSalesRowDto> result = new ArrayList<>();
        int rank = 1;
        for (Object[] r : rows) {
            BigDecimal amount = r[3] != null ? new BigDecimal(r[3].toString()) : BigDecimal.ZERO;
            BigDecimal share = grandTotal.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(grandTotal, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            result.add(ProductSalesRowDto.builder()
                    .rank(rank++)
                    .productId(r[0] != null ? ((Number) r[0]).longValue() : null)
                    .productName(r[1] != null ? r[1].toString() : null)
                    .totalQuantity(r[2] != null ? ((Number) r[2]).longValue() : 0L)
                    .totalAmount(amount)
                    .invoiceCount(r[4] != null ? ((Number) r[4]).longValue() : 0L)
                    .sharePercentage(share)
                    .build());
        }
        return result;
    }

    private List<DistributorOutstandingRowDto> buildDistributorOutstanding(
            List<Long> distIds, Long singleDistId, LocalDateTime from, LocalDateTime to) {
        List<Object[]> salesRows = invoiceRepository.getSalesByDistributor(from, to, PageRequest.of(0, 100));
        return salesRows.stream()
                .filter(r -> {
                    if (distIds != null) {
                        Long id = r[0] != null ? ((Number) r[0]).longValue() : null;
                        return id != null && distIds.contains(id);
                    }
                    return true;
                })
                .map(r -> {
                    Long distId = r[0] != null ? ((Number) r[0]).longValue() : null;
                    BigDecimal total = r[3] != null ? new BigDecimal(r[3].toString()) : BigDecimal.ZERO;
                    BigDecimal received = distId != null
                            ? orZero(paymentApprovalRepository.sumApprovedPaymentsByDistributorAndDateRange(distId, from, to))
                            : BigDecimal.ZERO;
                    return DistributorOutstandingRowDto.builder()
                            .distributorId(distId)
                            .distributorName(r[1] != null ? r[1].toString() : "Unknown")
                            .totalInvoices(r[2] != null ? ((Number) r[2]).longValue() : 0L)
                            .totalSales(total)
                            .receivedAmount(received)
                            .outstandingAmount(total.subtract(received).max(BigDecimal.ZERO))
                            .build();
                })
                .sorted((a, b) -> b.getOutstandingAmount().compareTo(a.getOutstandingAmount()))
                .collect(Collectors.toList());
    }

    private List<MisSalesTargetDto> buildWeeklyTarget(List<Object[]> rows) {
        Map<Integer, BigDecimal> actuals = new HashMap<>();
        for (Object[] r : rows) {
            int w = r[0] != null ? ((Number) r[0]).intValue() : 1;
            actuals.put(w, r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO);
        }
        List<MisSalesTargetDto> result = new ArrayList<>();
        for (int w = 1; w <= 5; w++) {
            result.add(MisSalesTargetDto.builder().period("Week " + w)
                    .actual(actuals.getOrDefault(w, BigDecimal.ZERO)).target(null).build());
        }
        return result;
    }

    private MisKpiCardDto kpiCard(BigDecimal current, BigDecimal previous) {
        BigDecimal change = null;
        if (previous != null && previous.compareTo(BigDecimal.ZERO) != 0) {
            change = current.subtract(previous)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previous, 2, RoundingMode.HALF_UP);
        }
        return MisKpiCardDto.builder()
                .amount(current)
                .changePercentage(change)
                .comparedTo("PREV_PERIOD")
                .build();
    }

    private BigDecimal totalStockValue() {
        return inventoryReportService.getSnapshot(null).stream()
                .map(s -> s.getTotalValue() != null ? s.getTotalValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumSales(Long distributorId, List<Long> distIds, LocalDateTime from, LocalDateTime to) {
        return distIds != null
                ? invoiceRepository.sumGrandTotalForDistributors(distIds, from, to)
                : invoiceRepository.sumGrandTotalByDistributorAndDateRange(distributorId, from, to);
    }

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private SalesInvoiceRowDto toDto(Invoice i) {
        return SalesInvoiceRowDto.builder()
                .id(i.getId()).invoiceNumber(i.getInvoiceNumber())
                .distributorId(i.getDistributorId()).distributorName(i.getDistributorName())
                .invoiceDate(i.getInvoiceDate()).totalAmount(i.getTotalAmount())
                .taxAmount(i.getTaxAmount()).grandTotal(i.getGrandTotal())
                .invoiceStatus(i.getInvoiceStatus() != null ? i.getInvoiceStatus().name() : null)
                .gdnNumber(i.getGdnNumber()).build();
    }

    private LocalDate resolveFrom(ReportFilterRequest f) {
        if (f.getStartDate() != null) return f.getStartDate();
        if (f.getFinancialYear() != null) return parseFyStart(f.getFinancialYear());
        return LocalDate.now().withDayOfYear(1);
    }

    private LocalDate resolveTo(ReportFilterRequest f) {
        if (f.getEndDate() != null) return f.getEndDate();
        if (f.getFinancialYear() != null) {
            LocalDate fyEnd = parseFyStart(f.getFinancialYear()).plusYears(1).minusDays(1);
            return fyEnd.isBefore(LocalDate.now()) ? fyEnd : LocalDate.now();
        }
        return LocalDate.now();
    }

    /** Parses "FY 2026-27" → 2026-04-01 */
    private LocalDate parseFyStart(String financialYear) {
        try {
            String digits = financialYear.replaceAll("[^0-9-]", "").trim();
            int startYear = Integer.parseInt(digits.split("-")[0]);
            return LocalDate.of(startYear, 4, 1);
        } catch (Exception e) {
            return LocalDate.now().withDayOfYear(1);
        }
    }
}
