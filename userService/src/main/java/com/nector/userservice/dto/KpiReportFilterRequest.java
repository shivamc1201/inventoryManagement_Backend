package com.nector.userservice.dto;

import com.nector.userservice.enums.KPIStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiReportFilterRequest {

    private Long employeeId;
    private Integer month;
    private Integer year;
    private Long kpiId;
    private KPIStatus status;
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private LocalDate endDateFrom;
    private LocalDate endDateTo;
    private String designation;
    private String roleName;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    private Integer page = 0;
    private Integer size = 20;
}
