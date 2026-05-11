package com.nector.userservice.model;

import com.nector.userservice.enums.KPIGrade;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_kpi_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeKpiResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Employee ID is required")
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    @Column(nullable = false)
    private Integer month;

    @Min(value = 2000, message = "Year must be 2000 or later")
    @Column(nullable = false)
    private Integer year;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_grade", length = 10)
    private KPIGrade finalGrade;

    @Size(max = 50, message = "Grade meaning cannot exceed 50 characters")
    @Column(name = "grade_meaning", length = 50)
    private String gradeMeaning;

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        if (totalScore == null) {
            totalScore = BigDecimal.ZERO;
        }
    }

    public void calculateGrade() {
        if (totalScore != null) {
            this.finalGrade = KPIGrade.fromScore(totalScore.doubleValue());
            this.gradeMeaning = this.finalGrade.getMeaning();
        }
    }
}
