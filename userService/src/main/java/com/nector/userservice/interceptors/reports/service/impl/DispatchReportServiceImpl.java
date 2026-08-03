package com.nector.userservice.interceptors.reports.service.impl;

import com.nector.userservice.dispatch.entity.Gdn;
import com.nector.userservice.dispatch.repository.GdnRepository;
import com.nector.userservice.interceptors.reports.dto.DispatchRegisterRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import com.nector.userservice.interceptors.reports.service.DispatchReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchReportServiceImpl implements DispatchReportService {

    private final GdnRepository gdnRepository;

    @Override
    public Page<DispatchRegisterRowDto> getRegister(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        PageRequest page = PageRequest.of(filter.getPage(), filter.getSize(), Sort.by("gdnDate").descending());
        return gdnRepository.findByGdnDateBetween(from, to, page).map(this::toDto);
    }

    @Override
    public Map<String, Object> getSummary(ReportFilterRequest filter) {
        LocalDateTime from = resolveFrom(filter).atStartOfDay();
        LocalDateTime to = resolveTo(filter).atTime(23, 59, 59);
        List<Object[]> rows = gdnRepository.getDispatchSummary(from, to);
        Map<String, Object> result = new HashMap<>();
        if (!rows.isEmpty()) {
            Object[] summary = rows.get(0);
            result.put("totalDispatches", summary[0]);
            result.put("totalWeight", summary[1]);
            result.put("totalPackages", summary[2]);
        }
        return result;
    }

    private DispatchRegisterRowDto toDto(Gdn g) {
        return DispatchRegisterRowDto.builder()
                .id(g.getId())
                .gdnNumber(g.getGdnNumber())
                .orderId(g.getOrderId())
                .gdnDate(g.getGdnDate())
                .vehicleNo(g.getVehicleNo())
                .transportName(g.getTransportName())
                .driverName(g.getDriverName())
                .driverMobile(g.getDriverMobile())
                .totalPackages(g.getTotalPackages())
                .totalWeight(g.getTotalWeight())
                .shippingAddress(g.getShippingAddress())
                .itemCount(g.getGdnItems() != null ? g.getGdnItems().size() : 0)
                .build();
    }

    private LocalDate resolveFrom(ReportFilterRequest f) {
        return f.getStartDate() != null ? f.getStartDate() : LocalDate.now().minusMonths(1);
    }

    private LocalDate resolveTo(ReportFilterRequest f) {
        return f.getEndDate() != null ? f.getEndDate() : LocalDate.now();
    }
}
