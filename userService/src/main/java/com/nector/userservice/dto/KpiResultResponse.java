package com.nector.userservice.dto;

import com.nector.userservice.enums.KPIGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiResultResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Integer month;
    private Integer year;
    private BigDecimal totalScore;
    private KPIGrade finalGrade;
    private String gradeMeaning;
    private LocalDateTime generatedAt;
}
