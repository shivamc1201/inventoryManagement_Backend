package com.nector.userservice.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * Utility class for Financial Year calculations
 * Financial Year runs from April 1 to March 31
 */
public class FinancialYearUtil {
    
    /**
     * Get current financial year in format "YYYY-YY"
     * Example: May 1, 2026 -> "2026-27"
     *          March 15, 2026 -> "2025-26"
     */
    public static String getCurrentFinancialYear() {
        LocalDate now = LocalDate.now();
        return getFinancialYear(now);
    }
    
    /**
     * Get financial year for a specific date in format "YYYY-YY"
     */
    public static String getFinancialYear(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        
        // If month is January to March (1-3), FY is previous year to current year
        if (month < Month.APRIL.getValue()) {
            int startYear = year - 1;
            int endYear = year;
            return String.format("%d-%02d", startYear, endYear % 100);
        } else {
            // If month is April to December (4-12), FY is current year to next year
            int startYear = year;
            int endYear = year + 1;
            return String.format("%d-%02d", startYear, endYear % 100);
        }
    }
    
    /**
     * Get start date of current financial year
     */
    public static LocalDate getFinancialYearStartDate() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        
        // If we're in Jan-Mar, FY started last year on April 1
        if (now.getMonthValue() < Month.APRIL.getValue()) {
            return LocalDate.of(year - 1, Month.APRIL, 1);
        } else {
            // If we're in Apr-Dec, FY started this year on April 1
            return LocalDate.of(year, Month.APRIL, 1);
        }
    }
    
    /**
     * Get end date of current financial year
     */
    public static LocalDate getFinancialYearEndDate() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        
        // If we're in Jan-Mar, FY ends this year on March 31
        if (now.getMonthValue() < Month.APRIL.getValue()) {
            return LocalDate.of(year, Month.MARCH, 31);
        } else {
            // If we're in Apr-Dec, FY ends next year on March 31
            return LocalDate.of(year + 1, Month.MARCH, 31);
        }
    }
}

