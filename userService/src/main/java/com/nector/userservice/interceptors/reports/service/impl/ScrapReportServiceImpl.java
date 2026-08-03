package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.dto.ScrapLifecycleRowDto;
import com.nector.userservice.interceptors.reports.service.ScrapReportService;
import com.nector.userservice.model.ScrapOutwardApproval;
import com.nector.userservice.repository.ScrapOutwardApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScrapReportServiceImpl implements ScrapReportService {

    private final ScrapOutwardApprovalRepository scrapApprovalRepository;

    @Override
    public List<ScrapLifecycleRowDto> getLifecycle(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return scrapApprovalRepository.findByRequestedOnBetweenOrderByRequestedOnDesc(from, to)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDisposalStatus(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return scrapApprovalRepository.getDisposalSummaryByStatus(from, to).stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("status", r[0]);
            m.put("count", r[1]);
            m.put("totalValue", r[2]);
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalRevenue(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        BigDecimal revenue = scrapApprovalRepository.getTotalScrapRevenue(from, to);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    private ScrapLifecycleRowDto toDto(ScrapOutwardApproval s) {
        BigDecimal totalValue = (s.getQuotedSellingPrice() != null && s.getQuantity() != null)
                ? s.getQuotedSellingPrice().multiply(BigDecimal.valueOf(s.getQuantity()))
                : BigDecimal.ZERO;
        return ScrapLifecycleRowDto.builder()
                .id(s.getId())
                .materialCode(s.getMaterialCode())
                .materialName(s.getMaterialName())
                .quantity(s.getQuantity())
                .quotedSellingPrice(s.getQuotedSellingPrice())
                .totalValue(totalValue)
                .issuedTo(s.getIssuedTo())
                .approvalStatus(s.getApprovalStatus())
                .requestedOn(s.getRequestedOn())
                .reviewedOn(s.getReviewedOn())
                .reviewedBy(s.getReviewedBy())
                .reviewComments(s.getReviewComments())
                .build();
    }

    private LocalDate resolveFrom(ReportFilterRequest f) {
        return f.getStartDate() != null ? f.getStartDate() : LocalDate.now().minusMonths(3);
    }

    private LocalDate resolveTo(ReportFilterRequest f) {
        return f.getEndDate() != null ? f.getEndDate() : LocalDate.now();
    }
}
