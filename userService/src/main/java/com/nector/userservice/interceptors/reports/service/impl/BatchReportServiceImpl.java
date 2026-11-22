package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.interceptors.reports.dto.BatchLifecycleDto;
import com.nector.userservice.interceptors.reports.service.BatchReportService;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.repository.FinishedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchReportServiceImpl implements BatchReportService {

    private final FinishedProductRepository finishedProductRepository;

    @Override
    public List<BatchLifecycleDto> getBatchLifecycle() {
        List<Object[]> rows = finishedProductRepository.getActiveBatchSummary();
        List<BatchLifecycleDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            String batchNumber = (String) r[0];
            List<FinishedProduct> products = finishedProductRepository.findByBatchNumberOrderByCreatedAtDesc(batchNumber);
            if (!products.isEmpty()) {
                FinishedProduct fp = products.get(0);
                boolean expired = fp.getExpiryDate() != null && fp.getExpiryDate().isBefore(LocalDate.now());
                result.add(BatchLifecycleDto.builder()
                        .batchNumber(batchNumber)
                        .productName(fp.getName())
                        .sku(fp.getSku())
                        .quantity(((Number) r[2]).intValue())
                        .expiryDate(fp.getExpiryDate())
                        .expired(expired)
                        .createdAt(fp.getCreatedAt())
                        .build());
            }
        }
        return result;
    }

    @Override
    public List<BatchLifecycleDto> getExpiryAlerts(int daysAhead) {
        LocalDate alertDate = LocalDate.now().plusDays(daysAhead);
        return finishedProductRepository.findExpiringBefore(alertDate).stream()
                .map(fp -> BatchLifecycleDto.builder()
                        .batchNumber(fp.getBatchNumber())
                        .productName(fp.getName())
                        .sku(fp.getSku())
                        .quantity(fp.getQuantity())
                        .expiryDate(fp.getExpiryDate())
                        .expired(fp.getExpiryDate().isBefore(LocalDate.now()))
                        .createdAt(fp.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
