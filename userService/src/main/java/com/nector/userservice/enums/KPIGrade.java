package com.nector.userservice.enums;

public enum KPIGrade {
    A_PLUS("A+", "Excellent", 90, 100),
    A("A", "Good Performance", 75, 89),
    B("B", "Average", 60, 74),
    C("C", "Improvement Needed", 0, 59);

    private final String grade;
    private final String meaning;
    private final int minScore;
    private final int maxScore;

    KPIGrade(String grade, String meaning, int minScore, int maxScore) {
        this.grade = grade;
        this.meaning = meaning;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getGrade() {
        return grade;
    }

    public String getMeaning() {
        return meaning;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public static KPIGrade fromScore(double score) {
        for (KPIGrade grade : values()) {
            if (score >= grade.minScore && score <= grade.maxScore) {
                return grade;
            }
        }
        return C;
    }
}
