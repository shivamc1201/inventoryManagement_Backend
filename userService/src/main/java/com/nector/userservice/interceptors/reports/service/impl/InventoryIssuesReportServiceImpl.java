package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.interceptors.reports.dto.InventoryIssueRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.service.InventoryIssuesReportService;
import com.nector.userservice.model.OutwardItemTransaction;
import com.nector.userservice.repository.OutwardItemTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryIssuesReportServiceImpl implements InventoryIssuesReportService {

    private final OutwardItemTransactionRepository outwardRepository;

    @Override
    public Page<InventoryIssueRowDto> getByType(String itemType, ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        PageRequest page = PageRequest.of(filter.getPage(), filter.getSize(), Sort.by("createdAt").descending());

        OutwardItemTransaction.ItemType type = parseItemType(itemType);
        return outwardRepository.findByItemTypeAndDateRange(type, from, to, page).map(this::toDto);
    }

    @Override
    public List<Map<String, Object>> getSummary(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        return outwardRepository.getIssueSummaryByType(from, to).stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("itemType", r[0] != null ? r[0].toString() : null);
            m.put("count", r[1]);
            m.put("totalQuantity", r[2]);
            return m;
        }).collect(Collectors.toList());
    }

    private InventoryIssueRowDto toDto(OutwardItemTransaction o) {
        return InventoryIssueRowDto.builder()
                .id(o.getId())
                .itemType(o.getItemType() != null ? o.getItemType().name() : null)
                .transactionType(o.getTransactionType() != null ? o.getTransactionType().name() : null)
                .materialCode(o.getMaterialCode())
                .materialName(o.getMaterialName())
                .quantity(o.getQuantity())
                .unit(o.getUnit() != null ? o.getUnit().name() : null)
                .issuedTo(o.getIssuedTo())
                .referenceNumber(o.getReferenceNumber())
                .comments(o.getComments())
                .quotedSellingPrice(o.getQuotedSellingPrice())
                .createdAt(o.getCreatedAt())
                .build();
    }

    private OutwardItemTransaction.ItemType parseItemType(String type) {
        if (type == null) return OutwardItemTransaction.ItemType.SPARE_PARTS;
        return switch (type.toUpperCase()) {
            case "PROMOTIONAL", "PROMOTIONAL_ITEMS" -> OutwardItemTransaction.ItemType.PROMOTIONAL_ITEMS;
            case "SCRAP", "SCRAP_MATERIAL" -> OutwardItemTransaction.ItemType.SCRAP_MATERIAL;
            default -> OutwardItemTransaction.ItemType.SPARE_PARTS;
        };
    }

    private LocalDate resolveFrom(ReportFilterRequest f) {
        return f.getStartDate() != null ? f.getStartDate() : LocalDate.now().minusMonths(1);
    }

    private LocalDate resolveTo(ReportFilterRequest f) {
        return f.getEndDate() != null ? f.getEndDate() : LocalDate.now();
    }
}
