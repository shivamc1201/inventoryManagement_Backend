package com.nector.userservice.interceptors.reports.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ReportFilterRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private String financialYear;
    private Long distributorId;
    private String reportType;
    private String groupBy;
    private int page = 0;
    private int size = 50;
}
