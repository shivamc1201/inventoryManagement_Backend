package com.nector.userservice.dto;

import com.nector.userservice.enums.KPIGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Yearly KPI summary for a single employee.
 * Contains month-by-month breakdown + overall yearly aggregate.
 * Used for hike / increment decisions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiYearlySummaryResponse {

    private Long employeeId;
    private String employeeName;
    private Integer year;

    /** Month-by-month results (only months that have been generated) */
    private List<KpiResultResponse> monthlyResults;

    /** Number of months recorded so far in this year */
    private Integer monthsRecorded;

    /** Average monthly score across all recorded months */
    private BigDecimal averageMonthlyScore;

    /** Overall yearly grade based on average monthly score */
    private KPIGrade yearlyGrade;

    /** Meaning of the yearly grade */
    private String yearlyGradeMeaning;

    /** Best month score */
    private BigDecimal bestMonthScore;

    /** Worst month score */
    private BigDecimal worstMonthScore;
}

