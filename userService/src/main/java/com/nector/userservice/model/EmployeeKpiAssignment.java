package com.nector.userservice.model;

import com.nector.userservice.enums.KPIStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_kpi_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeKpiAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Employee ID is required")
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @NotBlank(message = "Employee name is required")
    @Size(max = 255, message = "Employee name cannot exceed 255 characters")
    @Column(name = "employee_name", nullable = false, length = 255)
    private String employeeName;

    @Size(max = 100, message = "Designation cannot exceed 100 characters")
    @Column(length = 100)
    private String designation;

    @Size(max = 50, message = "Role name cannot exceed 50 characters")
    @Column(name = "role_name", length = 50)
    private String roleName;

    @NotNull(message = "KPI ID is required")
    @Column(name = "kpi_id", nullable = false)
    private Long kpiId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "kpi_id", referencedColumnName = "id", insertable = false, updatable = false)
    private KpiMaster kpiMaster;

    @NotNull(message = "Target value is required")
    @Min(value = 0, message = "Target value must be at least 0")
    @Column(name = "target_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetValue;

    @Min(value = 0, message = "Achieved value must be at least 0")
    @Column(name = "achieved_value", precision = 15, scale = 2)
    private BigDecimal achievedValue = BigDecimal.ZERO;

    @NotNull(message = "Weightage is required")
    @Min(value = 0, message = "Weightage must be at least 0")
    @Max(value = 100, message = "Weightage cannot exceed 100")
    @Column(nullable = false)
    private Integer weightage;

    @Column(name = "score_percentage", precision = 5, scale = 2)
    private BigDecimal scorePercentage = BigDecimal.ZERO;

    @Column(name = "weighted_score", precision = 5, scale = 2)
    private BigDecimal weightedScore = BigDecimal.ZERO;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private KPIStatus status = KPIStatus.ACTIVE;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    @Column(length = 500)
    private String remarks;

    /**
     * Month (1-12) this KPI assignment belongs to.
     * Auto-derived from startDate on persist/update.
     */
    @Column(name = "assigned_month")
    private Integer assignedMonth;

    /**
     * Year this KPI assignment belongs to.
     * Auto-derived from startDate on persist/update.
     */
    @Column(name = "assigned_year")
    private Integer assignedYear;

    @Column(name = "assigned_by")
    private Long assignedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (achievedValue == null) achievedValue = BigDecimal.ZERO;
        if (scorePercentage == null) scorePercentage = BigDecimal.ZERO;
        if (weightedScore == null) weightedScore = BigDecimal.ZERO;
        if (status == null) status = KPIStatus.ACTIVE;
        if (startDate != null) {
            this.assignedMonth = startDate.getMonthValue();
            this.assignedYear = startDate.getYear();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (startDate != null) {
            this.assignedMonth = startDate.getMonthValue();
            this.assignedYear = startDate.getYear();
        }
        calculateScores();
        checkCompletionStatus();
    }

    public void calculateScores() {
        if (targetValue != null && targetValue.compareTo(BigDecimal.ZERO) > 0 && achievedValue != null) {
            BigDecimal rawScore = achievedValue
                    .multiply(BigDecimal.valueOf(100))
                    .divide(targetValue, 2, RoundingMode.HALF_UP);

            // Cap at 100 — over-achievement doesn't inflate the score beyond full marks
            this.scorePercentage = rawScore.min(BigDecimal.valueOf(100));

            this.weightedScore = this.scorePercentage
                    .multiply(BigDecimal.valueOf(weightage))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
    }

    private void checkCompletionStatus() {
        if (achievedValue != null && targetValue != null && achievedValue.compareTo(targetValue) >= 0) {
            this.status = KPIStatus.COMPLETED;
        }
    }

    public void updateAchievedValue(BigDecimal achievedValue) {
        this.achievedValue = achievedValue;
        calculateScores();
        checkCompletionStatus();
    }
}
