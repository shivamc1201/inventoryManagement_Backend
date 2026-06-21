package com.nector.userservice.util;

import java.math.BigDecimal;

public class KpiGradeCalculator {

    public static GradeResult calculateGrade(BigDecimal scorePercentage) {
        if (scorePercentage == null) {
            return new GradeResult("C", "Improvement Needed");
        }

        double score = scorePercentage.doubleValue();
        
        if (score >= 90) {
            return new GradeResult("A+", "Excellent");
        } else if (score >= 75) {
            return new GradeResult("A", "Good Performance");
        } else if (score >= 60) {
            return new GradeResult("B", "Average");
        } else {
            return new GradeResult("C", "Improvement Needed");
        }
    }

    public static class GradeResult {
        private final String grade;
        private final String meaning;

        public GradeResult(String grade, String meaning) {
            this.grade = grade;
            this.meaning = meaning;
        }

        public String getGrade() {
            return grade;
        }

        public String getMeaning() {
            return meaning;
        }
    }
}
