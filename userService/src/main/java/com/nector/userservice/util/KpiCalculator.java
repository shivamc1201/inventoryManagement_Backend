package com.nector.userservice.util;

import com.nector.userservice.enums.KPIGrade;
import com.nector.userservice.model.EmployeeKpiAssignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@Slf4j
public class KpiCalculator {

    public BigDecimal calculateScorePercentage(BigDecimal achieved, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return achieved
                .multiply(BigDecimal.valueOf(100))
                .divide(target, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateWeightedScore(BigDecimal scorePercentage, Integer weightage) {
        if (weightage == null || weightage == 0) {
            return BigDecimal.ZERO;
        }
        
        return scorePercentage
                .multiply(BigDecimal.valueOf(weightage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalWeightedScore(List<EmployeeKpiAssignment> assignments) {
        return assignments.stream()
                .map(EmployeeKpiAssignment::getWeightedScore)
                .filter(ws -> ws != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public KPIGrade calculateGrade(BigDecimal totalScore) {
        return KPIGrade.fromScore(totalScore.doubleValue());
    }

    public BigDecimal calculateProgressPercentage(BigDecimal achieved, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal percentage = achieved
                .multiply(BigDecimal.valueOf(100))
                .divide(target, 2, RoundingMode.HALF_UP);
        
        // Cap at 100% for display purposes
        return percentage.min(BigDecimal.valueOf(100));
    }

    public BigDecimal calculateProjectedFinalScore(List<EmployeeKpiAssignment> assignments) {
        BigDecimal projectedTotal = BigDecimal.ZERO;
        
        for (EmployeeKpiAssignment assignment : assignments) {
            BigDecimal target = assignment.getTargetValue();
            BigDecimal achieved = assignment.getAchievedValue();
            Integer weightage = assignment.getWeightage();
            
            if (target != null && target.compareTo(BigDecimal.ZERO) > 0 && weightage != null) {
                // Assume 100% achievement for projection if not yet completed
                BigDecimal actualScore = achieved.multiply(BigDecimal.valueOf(100))
                        .divide(target, 2, RoundingMode.HALF_UP);
                BigDecimal projectedScore = actualScore.max(BigDecimal.valueOf(0)).min(BigDecimal.valueOf(100));
                
                BigDecimal weightedScore = projectedScore
                        .multiply(BigDecimal.valueOf(weightage))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                
                projectedTotal = projectedTotal.add(weightedScore);
            }
        }
        
        return projectedTotal.setScale(2, RoundingMode.HALF_UP);
    }
}
