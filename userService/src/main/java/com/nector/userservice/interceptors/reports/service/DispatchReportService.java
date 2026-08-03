package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.DispatchRegisterRowDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface DispatchReportService {
    Page<DispatchRegisterRowDto> getRegister(ReportFilterRequest filter);
    Map<String, Object> getSummary(ReportFilterRequest filter);
}
