package com.nector.userservice.exception;

public class KpiAssignmentException extends KpiException {
    
    public KpiAssignmentException(String message) {
        super(message);
    }
    
    public KpiAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static KpiAssignmentException duplicateAssignment(Long employeeId, Long kpiId) {
        return new KpiAssignmentException(
            "Employee " + employeeId + " already has an active assignment for KPI " + kpiId);
    }
    
    public static KpiAssignmentException weightageExceeded(Long employeeId, int currentWeightage, int newWeightage) {
        return new KpiAssignmentException(
            "Total weightage would exceed 100% for employee " + employeeId + 
            ". Current: " + currentWeightage + "%, New: " + newWeightage + "%");
    }
    
    public static KpiAssignmentException invalidDateRange() {
        return new KpiAssignmentException("End date must be after start date");
    }
}
